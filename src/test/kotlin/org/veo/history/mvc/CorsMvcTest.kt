/**
 * verinice.veo history
 * Copyright (C) 2021 Finn Westendorf
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
import org.junit.jupiter.api.Test
import org.springframework.http.HttpMethod

@WithMockClient()
class CorsMvcTest : AbstractMvcTest() {

    @Test
    fun `get my latest revisions with correct origin header`() {
        // given
        val origin = "https://valid.verinice.example"

        // when getting from the correct origin
        val result = request(
            HttpMethod.GET,
            "/revisions/my-latest/?owner=/units/deadbeef-5ca6-4935-ad01-c0ffe936beef",
            headers = mapOf("Origin" to listOf(origin))
        )

        // the request was successful
        result.response.status shouldBe 200
        result.response.contentAsString shouldBe "[]"
        result.response.getHeader("Access-Control-Allow-Origin") shouldBe origin
    }

    @Test
    fun `get revisions with wrong origin header`() {
        // given
        val origin = "https://invalid.notverinice.example"

        // when getting from the wrong origin
        val result = request(
            HttpMethod.GET,
            "/revisions/my-latest/?owner=/units/deadbeef-5ca6-4935-ad01-c0ffe936beef",
            headers = mapOf("Origin" to listOf(origin))
        )

        // then an error is returned
        result.response.status shouldBe 403
        result.response.contentAsString shouldBe "Invalid CORS request"
        result.response.getHeader("Access-Control-Allow-Origin") shouldBe null
    }

    @Test
    fun `pre-flight requests work`() {
        // given
        val origin = "https://valid.verinice.example"

        // when making a pre-flight request
        val result = request(
            HttpMethod.OPTIONS,
            "/",
            headers = mapOf(
                "Origin" to listOf(origin),
                "Access-Control-Request-Method" to listOf("GET"),
                "Access-Control-Request-Headers" to listOf("Authorization", "X-Ample", "X-Custom-Header")
            )
        )

        // then CORS headers are returned
        result.response.getHeader("Access-Control-Allow-Origin") shouldBe origin
        result.response.getHeader("Access-Control-Allow-Methods") shouldBe "GET,OPTIONS"
        result.response.getHeader("Access-Control-Allow-Headers") shouldBe "Authorization, X-Ample, X-Custom-Header"
        result.response.getHeader("Access-Control-Max-Age") shouldBe "1800"
    }
}
