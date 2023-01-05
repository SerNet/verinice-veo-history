/**
 * verinice.veo history
 * Copyright (C) 2020  Jonas Jordan
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
package org.veo.history.mvc

import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.http.HttpMethod.GET
import org.veo.history.Revision
import org.veo.history.RevisionType
import java.net.URI
import java.time.Instant
import java.util.UUID
import java.util.UUID.randomUUID

@WithMockClient
class PaginationMvcTest : AbstractMvcTest() {
    val clientId: UUID = UUID.fromString(mockClientUuid)

    @BeforeEach
    fun setup() {
        listOf(
            Revision(
                URI.create("/tech-stack"),
                RevisionType.CREATION,
                1,
                Instant.parse("2023-01-01T01:01:00.013621Z"),
                "dm",
                clientId,
                om.createObjectNode(),
            ),
            Revision(
                URI.create("/tech-stack"),
                RevisionType.MODIFICATION,
                2,
                Instant.parse("2023-01-01T01:02:00.013621Z"),
                "jj",
                clientId,
                om.createObjectNode(),
            ),
            Revision(
                URI.create("/back-pack"),
                RevisionType.CREATION,
                1,
                Instant.parse("2023-01-01T01:02:15.013621Z"),
                "jj",
                clientId,
                om.createObjectNode(),
            ),
            Revision(
                URI.create("/other-clients-stuff"),
                RevisionType.CREATION,
                1,
                Instant.parse("2023-01-01T01:04:15.013621Z"),
                "gk",
                randomUUID(),
                om.createObjectNode(),
            ),
        ).forEach(revisionRepo::add)
    }

    @Suppress("UNCHECKED_CAST")
    @Test
    fun `paginates through all client revisions`() {
        var firstRevisionId: String

        // expect that all client revisions are retrieved
        parseBody(request(GET, "/revisions/paged"))
            .let { it as Map<*, *> }
            .apply {
                get("totalItemCount") shouldBe 3
                get("items")
                    .let { it as List<Map<String, *>> }
                    .apply {
                        size shouldBe 3
                        firstRevisionId = get(0)["id"] as String

                        get(0)["uri"] shouldBe "/tech-stack"
                        get(0)["changeNumber"] shouldBe 1
                        get(1)["uri"] shouldBe "/tech-stack"
                        get(1)["changeNumber"] shouldBe 2
                        get(2)["uri"] shouldBe "/back-pack"
                        get(2)["changeNumber"] shouldBe 1
                    }
            }

        // and that a page size limit reduces the amount of items
        parseBody(request(GET, "/revisions/paged?size=2"))
            .let { it as Map<*, *> }
            .apply {
                get("totalItemCount") shouldBe 3
                get("items")
                    .let { it as List<Map<String, *>> }
                    .apply {
                        size shouldBe 2
                        get(0)["uri"] shouldBe "/tech-stack"
                        get(0)["changeNumber"] shouldBe 1
                        get(1)["uri"] shouldBe "/tech-stack"
                        get(1)["changeNumber"] shouldBe 2
                    }
            }

        // and that revisions can be skipped
        parseBody(request(GET, "/revisions/paged?afterId=$firstRevisionId"))
            .let { it as Map<*, *> }
            .apply {
                get("totalItemCount") shouldBe 3
                get("items")
                    .let { it as List<Map<String, *>> }
                    .apply {
                        size shouldBe 2
                        get(0)["uri"] shouldBe "/tech-stack"
                        get(0)["changeNumber"] shouldBe 2
                        get(1)["uri"] shouldBe "/back-pack"
                        get(1)["changeNumber"] shouldBe 1
                    }
            }
    }

    @Suppress("UNCHECKED_CAST")
    @Test
    fun `a default page size is applied`() {
        // given many additional revisions
        (1..100).forEach { i ->
            revisionRepo.add(Revision(URI.create("/important-resource"), RevisionType.MODIFICATION, i.toLong(), Instant.now(), "jj", clientId, om.createObjectNode()))
        }

        // expect that a limited amount is retrieved
        parseBody(request(GET, "/revisions/paged"))
            .let { it as Map<String, *> }
            .apply {
                get("totalItemCount") shouldBe 103
                get("items")
                    .let { it as List<*> }
                    .size shouldBe 20
            }
    }
}
