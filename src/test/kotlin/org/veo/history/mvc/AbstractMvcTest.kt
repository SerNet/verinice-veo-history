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

import groovy.json.JsonOutput
import groovy.json.JsonSlurper
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.http.HttpMethod
import org.springframework.scheduling.annotation.EnableAsync
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.MvcResult
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders

@EnableAsync
@AutoConfigureMockMvc
abstract class AbstractMvcTest : AbstractSpringTest() {

    @Autowired
    protected lateinit var mvc: MockMvc

    protected fun parseBody(result: MvcResult): Any {
        return JsonSlurper().parseText(result.response.contentAsString)
    }

    protected fun request(method: HttpMethod, url: String, body: Any? = null, headers: Map<String, List<String>> = emptyMap()): MvcResult {
        val request = MockMvcRequestBuilders.request(method, url)
        headers.forEach { k, v -> request.header(k, v) }
        if (body != null) {
            request
                .contentType("application/json")
                .content((JsonOutput.toJson(body)))
        }
        return mvc
            .perform(request)
            .andReturn()
    }
}
