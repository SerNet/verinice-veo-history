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
package org.veo.history.jpa

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.dao.DataIntegrityViolationException
import org.veo.history.Revision
import org.veo.history.RevisionType
import org.veo.history.mvc.AbstractSpringTest
import java.net.URI
import java.time.Instant
import java.util.UUID

class RevisionJpaRepoTest : AbstractSpringTest() {
    private val clientId = UUID.randomUUID()

    @Autowired
    private lateinit var sut: RevisionJpaRepo

    @BeforeEach
    fun setup() {
        // Four revisions by four different authors
        sut.save(
            Revision(
                URI.create("/foo/bar"),
                RevisionType.MODIFICATION,
                1,
                Instant.now(),
                "a",
                clientId,
                jsonObject("name" to "one"),
            ),
        )
        sut.save(
            Revision(
                URI.create("/foo/bar"),
                RevisionType.MODIFICATION,
                2,
                Instant.now(),
                "b",
                clientId,
                jsonObject("name" to "one"),
            ),
        )
        sut.save(
            Revision(
                URI.create("/foo/car"),
                RevisionType.MODIFICATION,
                1,
                Instant.now(),
                "c",
                clientId,
                jsonObject("name" to "two"),
            ),
        )
        sut.save(
            Revision(
                URI.create("/foo/car"),
                RevisionType.MODIFICATION,
                2,
                Instant.now(),
                "d",
                clientId,
                jsonObject("name" to "two"),
            ),
        )
    }

    @Test
    fun `can't add duplicate revision`() {
        shouldThrow<DataIntegrityViolationException> {
            sut.saveAndFlush(
                Revision(URI.create("/foo/car"), RevisionType.HARD_DELETION, 2, Instant.now(), "e", clientId, null),
            )
        }
    }

    @Test
    fun `finds all by URI`() {
        val uri = URI.create("/foo/bar")
        val result = sut.findAll(uri, clientId)

        result.size shouldBe 2
        result[0].author shouldBe "a"
        result[1].author shouldBe "b"

        // Result should be empty with wrong client ID.
        sut.findAll(uri, UUID.randomUUID()) shouldBe emptyList()
    }

    @Test
    fun `finds revision by URI and changeNumber`() {
        val uri = URI.create("/foo/car")
        val changeNumber = 2L
        val result = sut.find(uri, changeNumber, clientId)

        result?.author shouldBe "d"

        // Result should be empty with wrong client ID
        sut.find(uri, changeNumber, UUID.randomUUID()) shouldBe null
    }

    @Test
    fun `finds contemporary revisions`() {
        val uri = URI.create("/contemporary-test")
        sut.save(
            Revision(
                uri,
                RevisionType.MODIFICATION,
                1,
                Instant.parse("2021-05-04T10:00:00.000000Z"),
                "a",
                clientId,
                jsonObject(),
            ),
        )
        sut.save(
            Revision(
                uri,
                RevisionType.MODIFICATION,
                2,
                Instant.parse("2021-05-04T11:00:00.000000Z"),
                "a",
                clientId,
                jsonObject(),
            ),
        )
        sut.save(
            Revision(
                uri,
                RevisionType.MODIFICATION,
                3,
                Instant.parse("2021-05-04T12:00:00.000000Z"),
                "a",
                clientId,
                jsonObject(),
            ),
        )
        sut.save(
            Revision(
                uri,
                RevisionType.MODIFICATION,
                4,
                Instant.parse("2021-05-04T13:00:00.000000Z"),
                "a",
                clientId,
                jsonObject(),
            ),
        )

        val uriAsString = uri.toString()
        sut.find(uriAsString, Instant.parse("2021-05-04T11:00:00.000000Z"), clientId)
            ?.changeNumber shouldBe 2
        sut.find(uriAsString, Instant.parse("2021-05-04T11:30:00.000000Z"), clientId)
            ?.changeNumber shouldBe 2
        sut.find(uriAsString, Instant.parse("2021-05-04T11:59:59.999999Z"), clientId)
            ?.changeNumber shouldBe 2
        sut.find(uriAsString, Instant.parse("2021-05-04T12:00:00.000000Z"), clientId)
            ?.changeNumber shouldBe 3

        // Result should be empty with wrong client ID
        sut.find(uriAsString, Instant.parse("2021-05-04T12:00:00.000000Z"), UUID.randomUUID()) shouldBe null
    }

    @Test
    fun `finds user revisions by owner`() {
        // Two revisions that should match
        sut.save(
            Revision(
                URI.create("/my-new-resource"),
                RevisionType.CREATION,
                1,
                Instant.parse("2021-05-04T09:00:00.000000Z"),
                "thisUser",
                clientId,
                jsonObject("owner" to mapOf("targetUri" to "/owner/1")),
            ),
        )
        sut.save(
            Revision(
                URI.create("/my-updated-resource"),
                RevisionType.MODIFICATION,
                2,
                Instant.parse("2021-05-04T11:05:00.000000Z"),
                "thisUser",
                clientId,
                jsonObject("owner" to mapOf("targetUri" to "/owner/1")),
            ),
        )

        // Shouldn't match because it's not the latest revision of that resource
        sut.save(
            Revision(
                URI.create("/my-updated-resource"),
                RevisionType.CREATION,
                1,
                Instant.parse("2021-05-04T10:00:00.000000Z"),
                "thisUser",
                clientId,
                jsonObject("owner" to mapOf("targetUri" to "/owner/1")),
            ),
        )

        // Shouldn't match because it's the wrong author
        sut.save(
            Revision(
                URI.create("/resource-updated-by-somebody-else"),
                RevisionType.MODIFICATION,
                1,
                Instant.parse("2021-05-04T12:00:00.000000Z"),
                "anotherUser",
                clientId,
                jsonObject("owner" to mapOf("targetUri" to "/owner/1")),
            ),
        )

        // Shouldn't match because it's the wrong owner
        sut.save(
            Revision(
                URI.create("/other-owners-resource"),
                RevisionType.MODIFICATION,
                2,
                Instant.parse("2021-05-04T13:00:00.000000Z"),
                "thisUser",
                clientId,
                jsonObject("owner" to mapOf("targetUri" to "/owner/2")),
            ),
        )

        // Shouldn't match because it's the wrong client (kind of unrealistic but just to be sure)
        sut.save(
            Revision(
                URI.create("/other-clients-resource"),
                RevisionType.MODIFICATION,
                1,
                Instant.parse("2021-05-04T14:00:00.000000Z"),
                "thisUser",
                UUID.randomUUID(),
                jsonObject("owner" to mapOf("targetUri" to "/owner/1")),
            ),
        )

        // Shouldn't match because the resource is deleted
        sut.save(
            Revision(
                URI.create("/my-deleted-resource"),
                RevisionType.CREATION,
                1,
                Instant.parse("2021-05-04T15:00:00.000000Z"),
                "thisUser",
                clientId,
                jsonObject("owner" to mapOf("targetUri" to "/owner/1")),
            ),
        )
        sut.save(
            Revision(
                URI.create("/my-deleted-resource"),
                RevisionType.HARD_DELETION,
                2,
                Instant.parse("2021-05-04T16:00:00.000000Z"),
                "thisUser",
                clientId,
                null,
            ),
        )

        val result = sut.findMostRecentlyChangedResources("thisUser", "/owner/1", clientId)

        result.size shouldBe 2
        result.forEach {
            it.clientId shouldBe clientId
            it.author shouldBe "thisUser"
            it.content!!.get("owner")?.get("targetUri")?.asText() shouldBe "/owner/1"
        }
        result[0].let {
            it.uri shouldBe URI.create("/my-updated-resource")
            it.changeNumber shouldBe 2
        }
        result[1].let {
            it.uri shouldBe URI.create("/my-new-resource")
            it.changeNumber shouldBe 1
        }
    }

    @Test
    fun `finds revision by name in content`() {
        val name = "one"
        val result = sut.find(name, clientId)

        result.map { it.author } shouldContainExactlyInAnyOrder listOf("a", "b")

        // Result should be empty with wrong client ID
        sut.find(name, UUID.randomUUID()).size shouldBe 0
    }

    @Test
    fun `deletes all client revisions`() {
        // Given another client with one revision
        val otherClientId = UUID.randomUUID()
        sut.save(
            Revision(
                URI.create("/goo/star"),
                RevisionType.CREATION,
                1,
                Instant.parse("2021-05-04T15:00:00.000000Z"),
                "otherUser",
                otherClientId,
                jsonObject(),
            ),
        )

        // when deleting the original client's revisions
        sut.deleteAllClientRevisions(clientId)

        // then they are gone
        sut.findAll(URI.create("/foo/bar"), clientId).size shouldBe 0
        sut.findAll(URI.create("/foo/car"), clientId).size shouldBe 0

        // and the other client's revision remains
        sut.findAll(URI.create("/goo/star"), otherClientId).size shouldBe 1
    }
}
