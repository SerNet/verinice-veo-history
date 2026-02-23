/*
 * verinice.veo history
 * Copyright (C) 2021  Jonas Jordan
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.veo.history

import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.amqp.AmqpRejectAndDontRequeueException
import org.springframework.amqp.rabbit.annotation.Argument
import org.springframework.amqp.rabbit.annotation.Exchange
import org.springframework.amqp.rabbit.annotation.Queue
import org.springframework.amqp.rabbit.annotation.QueueBinding
import org.springframework.amqp.rabbit.annotation.RabbitListener
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.stereotype.Component
import tools.jackson.databind.JsonNode
import tools.jackson.databind.ObjectMapper
import java.net.URI
import java.time.Instant
import java.util.UUID

private val log = KotlinLogging.logger {}

@Component
@ConditionalOnProperty(value = ["veo.history.rabbitmq.subscribe"], havingValue = "true")
class MessageSubscriber(
    private val revisionRepo: RevisionRepo,
) {
    private val mapper = ObjectMapper()

    @RabbitListener(
        bindings = [
            QueueBinding(
                value =
                    Queue(
                        value = "\${veo.history.rabbitmq.queues.veo}",
                        exclusive = "false",
                        durable = "true",
                        autoDelete = "false",
                        arguments = [Argument(name = "x-dead-letter-exchange", value = "\${veo.history.rabbitmq.dlx}")],
                    ),
                exchange = Exchange(value = "\${veo.history.rabbitmq.exchanges.veo}", type = "topic"),
                key = [
                    "\${veo.history.rabbitmq.routing_key_prefix}entity_revision",
                ],
            ),
        ],
    )
    fun handleVeoMessage(message: String) {
        handle(
            message,
            mapOf(
                "entity_revision" to this::handleVersioning,
            ),
        )
    }

    @RabbitListener(
        bindings = [
            QueueBinding(
                value =
                    Queue(
                        value = "\${veo.history.rabbitmq.queues.veo-subscriptions}",
                        exclusive = "false",
                        durable = "true",
                        autoDelete = "false",
                        arguments = [Argument(name = "x-dead-letter-exchange", value = "\${veo.history.rabbitmq.dlx}")],
                    ),
                exchange = Exchange(value = "\${veo.history.rabbitmq.exchanges.veo-subscriptions}", type = "topic"),
                key = [
                    "\${veo.history.rabbitmq.routing_key_prefix}client_change",
                ],
            ),
        ],
    )
    fun handleSubscriptionMessage(message: String) {
        handle(
            message,
            mapOf(
                "client_change" to this::handleClientChange,
            ),
        )
    }

    private fun handle(
        message: String,
        eventTypeHandlers: Map<String, (JsonNode) -> Any>,
    ) = try {
        mapper
            .readTree(message)
            .get("content")
            .asString()
            .let(mapper::readTree)
            .let { content ->
                val eventType = content.get("eventType").asString()
                log.debug { "Received message with '$eventType' event" }
                eventTypeHandlers[eventType]
                    ?.also { handler -> handler(content) }
                    ?: throw NotImplementedError("Unsupported event type '$eventType'")
            }
    } catch (ex: AmqpRejectAndDontRequeueException) {
        throw ex
    } catch (ex: Throwable) {
        log.error(ex) { "Handling failed for message: '$message'" }
        throw RuntimeException(ex)
    }

    private fun handleClientChange(content: JsonNode) {
        if (content.get("type").asString() == "DELETION") {
            val clientId = UUID.fromString(content.get("clientId").asString())
            log.info { "Deleting all revisions owned by client $clientId" }
            revisionRepo.deleteAllClientRevisions(clientId)
        }
    }

    private fun handleVersioning(content: JsonNode) =
        content
            .run {
                val revisionType = RevisionType.valueOf(get("type").asString())
                val content = get("content")
                if (revisionType != RevisionType.HARD_DELETION && content == null) {
                    val uri = get("uri").asString()
                    log.warn { "Received versioning message with type $revisionType and no content for resource $uri" }
                    throw AmqpRejectAndDontRequeueException("content is required for revision type $revisionType")
                }
                Revision(
                    URI.create(get("uri").asString()),
                    revisionType,
                    get("changeNumber").asLong(),
                    Instant.parse(get("time").asString()),
                    get("author").asString(),
                    UUID.fromString(get("clientId").asString()),
                    content,
                )
            }.apply {
                try {
                    revisionRepo.add(this)
                } catch (ex: DuplicateRevisionException) {
                    log.debug(ex) { "Duplicate revision" }
                    log.warn { "Ignoring duplicated versioning message: change number $changeNumber of resource $uri" }
                    throw AmqpRejectAndDontRequeueException(ex.message!!)
                }
            }
}
