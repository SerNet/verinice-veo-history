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
import org.springframework.http.HttpMethod
import org.veo.history.Revision
import org.veo.history.RevisionType
import java.net.URI
import java.time.Instant
import java.util.UUID

@WithMockClient
class RevisionMvcTest : AbstractMvcTest() {
    private val resourceUri = "/processes/85773f48-d7bb-4605-a0fe-9db6f1db5b82"

    @BeforeEach
    fun setup() {
        val clientId = UUID.fromString(MOCK_CLIENT_UUID)
        listOf(
            Revision(
                URI.create(resourceUri),
                RevisionType.CREATION,
                1,
                Instant.parse("2021-01-27T11:27:00.013621Z"),
                "dm",
                clientId,
                jsonObject(
                    "name" to "Process 1",
                    "owner" to
                        mapOf(
                            "targetUri" to "/owners/1",
                        ),
                ),
            ),
            Revision(
                URI.create(resourceUri),
                RevisionType.MODIFICATION,
                2,
                Instant.parse("2021-01-28T11:27:00.013621Z"),
                "jj",
                clientId,
                jsonObject(
                    "name" to "Super Process 1",
                    "owner" to mapOf("targetUri" to "/owners/1"),
                ),
            ),
            Revision(
                URI.create(resourceUri),
                RevisionType.MODIFICATION,
                3,
                Instant.parse("2021-01-29T11:27:00.013621Z"),
                "jj",
                clientId,
                jsonObject(
                    "name" to "Mega Process 1",
                    "owner" to mapOf("targetUri" to "/owners/1"),
                ),
            ),
            Revision(
                URI.create(resourceUri),
                RevisionType.MODIFICATION,
                4,
                Instant.parse("2021-01-30T11:27:00.013621Z"),
                "jk",
                clientId,
                jsonObject(
                    "name" to "Ultra Process 1",
                    "owner" to mapOf("targetUri" to "/owners/1"),
                ),
            ),
            Revision(
                URI.create(resourceUri),
                RevisionType.HARD_DELETION,
                5,
                Instant.parse("2021-01-30T11:27:00.013621Z"),
                "dm",
                clientId,
                jsonObject(
                    "name" to "Ultra Process 1",
                    "owner" to mapOf("targetUri" to "/owners/1"),
                ),
            ),
        ).forEach {
            revisionRepo.add(it)
        }
    }

    @Test
    fun retrievesAllMockRevisions() {
        val result = parseBody(request(HttpMethod.GET, "/revisions?uri=$resourceUri"))
        (result as List<*>).apply {
            size shouldBe 5
            (get(0) as Map<*, *>).apply {
                get("changeNumber") shouldBe 1
                get("type") shouldBe "CREATION"
                get("author") shouldBe "dm"
                (get("content") as Map<*, *>).apply {
                    get("name") shouldBe "Process 1"
                }
            }
            (get(1) as Map<*, *>).apply {
                get("changeNumber") shouldBe 2
                get("type") shouldBe "MODIFICATION"
                get("author") shouldBe "jj"
                (get("content") as Map<*, *>).apply {
                    get("name") shouldBe "Super Process 1"
                }
            }
            (get(2) as Map<*, *>).apply {
                get("changeNumber") shouldBe 3
                get("type") shouldBe "MODIFICATION"
                get("author") shouldBe "jj"
                (get("content") as Map<*, *>).apply {
                    get("name") shouldBe "Mega Process 1"
                }
            }
            (get(3) as Map<*, *>).apply {
                get("changeNumber") shouldBe 4
                get("type") shouldBe "MODIFICATION"
                get("author") shouldBe "jk"
                (get("content") as Map<*, *>).apply {
                    get("name") shouldBe "Ultra Process 1"
                }
            }
            (get(4) as Map<*, *>).apply {
                get("changeNumber") shouldBe 5
                get("type") shouldBe "HARD_DELETION"
                get("author") shouldBe "dm"
            }
        }
    }

    @Test
    fun retrievesRevisionByChangeNumber() {
        val result = parseBody(request(HttpMethod.GET, "/revisions/change/2?uri=$resourceUri"))
        (result as Map<*, *>).apply {
            get("changeNumber") shouldBe 2
            get("author") shouldBe "jj"
        }
    }

    @Test
    fun retrievesContemporaryRevision() {
        val result =
            parseBody(request(HttpMethod.GET, "/revisions/contemporary/2021-01-30T08:12:34.567890Z?uri=$resourceUri"))
        (result as Map<*, *>).apply {
            get("changeNumber") shouldBe 3
            get("author") shouldBe "jj"
        }
    }

    @Test
    @WithMockClient("jj")
    fun retrievesMostRecentlyChangedResources() {
        val result = parseBody(request(HttpMethod.GET, "/revisions/my-latest?owner=/owners/1"))
        (result as List<*>).apply {
            size shouldBe 1
            (first() as Map<*, *>).apply {
                get("author") shouldBe "jj"
                get("uri") shouldBe resourceUri
                get("changeNumber") shouldBe 3
            }
        }
    }
}
