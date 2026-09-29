/*
 * verinice.veo history
 * Copyright (C) 2026  Jochen Kemnade
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

import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotest.matchers.collections.shouldNotContain
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import org.junit.jupiter.api.Test
import org.springframework.http.HttpMethod.GET

class OpenApiMvcTest : AbstractMvcTest() {
    @Test
    fun `revision content is documented as a nullable JSON object`() {
        val result = request(GET, "/v3/api-docs")
        result.response.status shouldBe 200

        val schemas = (parseBody(result) as Map<*, *>).path("components", "schemas")
        val content = schemas.path("RevisionDto", "properties", "content")

        content["description"] shouldBe "Resource body at this revision"
        content["type"].shouldBeInstanceOf<List<*>>() shouldContainExactlyInAnyOrder listOf("object", "null")

        // The internal representation of the content must not leak into the docs.
        content.keys shouldNotContain "\$ref"
        content.keys shouldNotContain "properties"
        schemas.keys shouldNotContain "JsonNode"
    }

    private fun Map<*, *>.path(vararg keys: String): Map<*, *> =
        keys.fold(this) { node, key ->
            node[key] as? Map<*, *> ?: error("OpenAPI docs lack object '$key' on path ${keys.joinToString(".")}")
        }
}
