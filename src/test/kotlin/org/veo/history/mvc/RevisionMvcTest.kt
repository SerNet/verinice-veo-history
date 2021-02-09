/**
 * Copyright (c) 2020 Jonas Jordan.
 *
 * This program is free software: you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public License
 * as published by the Free Software Foundation, either version 3
 * of the License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.
 * If not, see <http://www.gnu.org/licenses/>.
 */
package org.veo.history.mvc

import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test
import org.springframework.http.HttpMethod

@WithMockClient
class RevisionMvcTest : AbstractMvcTest() {
    @Test
    fun retrievesAllMockRevisions() {
        val result = parseBody(request(HttpMethod.GET, "/revisions?url=https://test.dev/foo"))
        (result as List<*>).apply {
            size shouldBe 5
            (get(0) as Map<*, *>).apply {
                get("version") shouldBe 1
                get("type") shouldBe "CREATION"
                get("author") shouldBe "dm"
            }
            (get(1) as Map<*, *>).apply {
                get("version") shouldBe 2
                get("type") shouldBe "MODIFICATION"
                get("author") shouldBe "jj"
            }
            (get(2) as Map<*, *>).apply {
                get("version") shouldBe 3
                get("type") shouldBe "MODIFICATION"
                get("author") shouldBe "jj"
            }
            (get(3) as Map<*, *>).apply {
                get("version") shouldBe 4
                get("type") shouldBe "MODIFICATION"
                get("author") shouldBe "jk"
            }
            (get(4) as Map<*, *>).apply {
                get("version") shouldBe 5
                get("type") shouldBe "SOFT_DELETION"
                get("author") shouldBe "dm"
            }
        }
    }

    @Test
    fun retrievesRevisionByVersion() {
        val result = parseBody(request(HttpMethod.GET, "/revisions/version/2?url=https://test.dev/foo"))
        (result as Map<*, *>).apply {
            get("version") shouldBe 2
            get("author") shouldBe "jj"
        }
    }

    @Test
    fun retrievesContemporaryRevision() {
        val result = parseBody(request(HttpMethod.GET, "/revisions/contemporary/2021-01-30T08:12:34.567890Z?url=https://test.dev/foo"))
        (result as Map<*, *>).apply {
            get("version") shouldBe 3
            get("author") shouldBe "jj"
        }
    }
}
