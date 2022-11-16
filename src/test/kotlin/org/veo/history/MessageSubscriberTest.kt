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

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import io.mockk.slot
import org.junit.jupiter.api.Test
import org.springframework.amqp.AmqpRejectAndDontRequeueException
import java.io.IOException
import java.net.URI
import java.time.Instant
import java.util.UUID

class MessageSubscriberTest {
    private val repoMock: RevisionRepo = mockk()
    private val sut = MessageSubscriber(repoMock)

    private val creationMessage =
        "{\"routingKey\":\"nullversioning_event\",\"content\":\"{\\\"uri\\\":\\\"/units/7e33c300-da43-4a82-b21b-fa4b89c023e5\\\",\\\"type\\\":\\\"CREATION\\\",\\\"changeNumber\\\":0,\\\"time\\\":\\\"2021-04-16T09:54:54.871021Z\\\",\\\"author\\\":\\\"veo-testuser1\\\",\\\"clientId\\\":\\\"21712604-ed85-4f08-aa46-1cf39607ee9e\\\",\\\"content\\\":{\\\"name\\\":\\\"My unit\\\",\\\"createdAt\\\":\\\"2021-04-16T09:54:54.871021Z\\\",\\\"createdBy\\\":\\\"veo-testuser1\\\",\\\"updatedAt\\\":\\\"2021-04-16T09:54:54.871021Z\\\",\\\"updatedBy\\\":\\\"veo-testuser1\\\",\\\"units\\\":[],\\\"domains\\\":[{\\\"displayName\\\":\\\"Placeholder domain - see issue VEO-227\\\",\\\"targetUri\\\":\\\"/domains/3f8ef603-ec02-40f9-ba4d-01b66f0ee88d\\\"}],\\\"id\\\":\\\"7e33c300-da43-4a82-b21b-fa4b89c023e5\\\"}}\",\"id\":10,\"timestamp\":\"2021-04-16T09:54:54.874040Z\"}"

    @Test
    fun `adds versioning event to repo`() {
        val revisionSlot = slot<Revision>()
        every { repoMock.add(capture(revisionSlot)) } just runs

        sut.handleEntityEvent(creationMessage)

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
    fun `rejects but doesn't requeue message if it's a duplicate`() {
        every { repoMock.add(any()) } throws DuplicateRevisionException(URI.create("/units/7e33c300-da43-4a82-b21b-fa4b89c023e5"), 0)

        shouldThrow<AmqpRejectAndDontRequeueException> {
            sut.handleEntityEvent(creationMessage)
        }
    }

    @Test
    fun `delegates other repo exceptions`() {
        every { repoMock.add(any()) } throws IOException("I can't save that stuff.")

        shouldThrow<IOException> {
            sut.handleEntityEvent(creationMessage)
        }
    }
}
