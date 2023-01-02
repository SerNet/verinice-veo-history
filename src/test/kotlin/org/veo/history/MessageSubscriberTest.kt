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
import io.kotest.assertions.throwables.shouldNotThrowAny
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import io.mockk.Runs
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import io.mockk.slot
import io.mockk.verify
import org.junit.jupiter.api.Test
import org.springframework.amqp.AmqpRejectAndDontRequeueException
import java.io.IOException
import java.net.URI
import java.time.Instant
import java.util.UUID

private val om = ObjectMapper()

class MessageSubscriberTest {
    private val repoMock: RevisionRepo = mockk()
    private val sut = MessageSubscriber(repoMock)

    private val creationMessage = message(
        mapOf(
            "uri" to "/units/7e33c300-da43-4a82-b21b-fa4b89c023e5",
            "type" to "CREATION",
            "changeNumber" to 0,
            "time" to "2021-04-16T09:54:54.871021Z",
            "author" to "veo-testuser1",
            "clientId" to "21712604-ed85-4f08-aa46-1cf39607ee9e",
            "content" to mapOf(
                "name" to "My unit",
                "createdAt" to "2021-04-16T09:54:54.871021Z",
                "createdBy" to "veo-testuser1",
                "updatedAt" to "2021-04-16T09:54:54.871021Z",
                "updatedBy" to "veo-testuser1",
                "units" to emptyList<Any>(),
                "domains" to listOf(
                    mapOf(
                        "displayName" to "Placeholder domain - see issue VEO-227",
                        "targetUri" to "/domains/3f8ef603-ec02-40f9-ba4d-01b66f0ee88d",
                    ),
                ),
            ),
            "id" to "7e33c300-da43-4a82-b21b-fa4b89c023e5",
        ),
    )

    @Test
    fun `adds versioning event to repo`() {
        val revisionSlot = slot<Revision>()
        every { repoMock.add(capture(revisionSlot)) } just runs

        sut.handleMessage(creationMessage)

        revisionSlot.captured.apply {
            uri shouldBe URI.create("/units/7e33c300-da43-4a82-b21b-fa4b89c023e5")
            type shouldBe RevisionType.CREATION
            changeNumber shouldBe 0
            time shouldBe Instant.parse("2021-04-16T09:54:54.871021Z")
            author shouldBe "veo-testuser1"
            clientId shouldBe UUID.fromString("21712604-ed85-4f08-aa46-1cf39607ee9e")
            content.get("name").asText() shouldBe "My unit"
        }
    }

    @Test
    fun `rejects but does not requeue message if it is a duplicate`() {
        every { repoMock.add(any()) } throws DuplicateRevisionException(
            URI.create("/units/7e33c300-da43-4a82-b21b-fa4b89c023e5"),
            0,
        )

        shouldThrow<AmqpRejectAndDontRequeueException> {
            sut.handleMessage(creationMessage)
        }
    }

    @Test
    fun `delegates other repo exceptions`() {
        every { repoMock.add(any()) } throws IOException("I can't save that stuff.")

        shouldThrow<IOException> {
            sut.handleMessage(creationMessage)
        }
    }

    @Test
    fun `deletes client revisions`() {
        every { repoMock.deleteAllClientRevisions(any()) } just Runs

        sut.handleMessage(
            message(
                mapOf(
                    "eventType" to "client_change",
                    "clientId" to "21712604-ed85-4f08-aa46-1cf39607ee9e",
                    "type" to "DELETION",
                ),
            ),
        )

        verify { repoMock.deleteAllClientRevisions(UUID.fromString("21712604-ed85-4f08-aa46-1cf39607ee9e")) }
    }

    @Test
    fun `ignores client creation`() {
        shouldNotThrowAny {
            sut.handleMessage(
                message(
                    mapOf(
                        "eventType" to "client_change",
                        "clientId" to "21712604-ed85-4f08-aa46-1cf39607ee9e",
                        "type" to "CREATION",
                    ),
                ),
            )
        }
    }

    private fun message(content: Map<String, *>): String =
        content
            .let(om::writeValueAsString)
            .let { mapOf("content" to it) }
            .let(om::writeValueAsString)
}
