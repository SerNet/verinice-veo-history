/**
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

import com.fasterxml.jackson.databind.ObjectMapper
import mu.KotlinLogging
import org.springframework.amqp.AmqpRejectAndDontRequeueException
import org.springframework.amqp.rabbit.annotation.Argument
import org.springframework.amqp.rabbit.annotation.Exchange
import org.springframework.amqp.rabbit.annotation.Queue
import org.springframework.amqp.rabbit.annotation.QueueBinding
import org.springframework.amqp.rabbit.annotation.RabbitListener
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.stereotype.Component
import java.net.URI
import java.time.Instant
import java.util.UUID

private val log = KotlinLogging.logger {}

@Component
@ConditionalOnProperty(value = ["veo.history.rabbitmq.subscribe"], havingValue = "true")
class EventSubscriber(private val revisionRepo: RevisionRepo) {
    private val mapper = ObjectMapper()

    @RabbitListener(
        bindings = [
            QueueBinding(
                value = Queue(
                    value = "\${veo.history.rabbitmq.queue}",
                    exclusive = "false",
                    durable = "true",
                    autoDelete = "false",
                    arguments = [Argument(name = "x-dead-letter-exchange", value = "\${veo.history.rabbitmq.dlx}")]
                ),
                exchange = Exchange(value = "\${veo.history.rabbitmq.exchange}", type = "topic"),
                key = ["\${veo.history.rabbitmq.routing_key_prefix}versioning_event"]
            )
        ]
    )
    fun handleEntityEvent(message: String) {
        mapper.readTree(message)
            .apply { log.debug { "Received entity event message with ID ${get("id").asText()}" } }
            .run { mapper.readTree(get("content").asText()) }
            .run {
                Revision(
                    URI.create(get("uri").asText()),
                    RevisionType.valueOf(get("type").asText()),
                    get("changeNumber").asLong(),
                    Instant.parse(get("time").asText()),
                    get("author").asText(),
                    UUID.fromString(get("clientId").asText()),
                    get("content")
                )
            }
            .apply {
                try {
                    revisionRepo.add(this)
                } catch (ex: DuplicateRevisionException) {
                    log.debug("Duplicate revision", ex)
                    log.warn { "Ignoring duplicated versioning message: change number $changeNumber of resource $uri" }
                    throw AmqpRejectAndDontRequeueException(ex.message)
                }
            }
    }
}
