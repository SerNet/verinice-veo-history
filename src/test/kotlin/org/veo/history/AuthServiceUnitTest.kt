/**
 * verinice.veo reporting
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
package org.veo.history

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import java.lang.IllegalArgumentException
import java.util.UUID
import org.junit.jupiter.api.Test
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken

class AuthServiceUnitTest {
    private val sut = AuthService()

    @Test
    fun `parses client UUID`() {
        val auth = mockk<JwtAuthenticationToken> {
            every { token } returns mockk {
                every { getClaimAsStringList("groups") } returns listOf(
                        "/veo_client:76ca215f-f4e3-4cbd-8524-f69742cc4dad")
            }
        }

        val clientId = sut.getClientId(auth)

        clientId shouldBe UUID.fromString("76ca215f-f4e3-4cbd-8524-f69742cc4dad")
    }

    @Test
    fun `parses client UUID with mixed groups`() {
        val auth = mockk<JwtAuthenticationToken> {
            every { token } returns mockk {
                every { getClaimAsStringList("groups") } returns listOf(
                        "keycloak-maintainer",
                        "/veo_client:76ca215f-f4e3-4cbd-8524-f69742cc4dad")
            }
        }

        val clientId = sut.getClientId(auth)

        clientId shouldBe UUID.fromString("76ca215f-f4e3-4cbd-8524-f69742cc4dad")
    }

    @Test
    fun `throws exception for multiple group claims`() {
        val auth = mockk<JwtAuthenticationToken> {
            every { token } returns mockk {
                every { getClaimAsStringList("groups") } returns listOf(
                        "/veo_client:76ca215f-f4e3-4cbd-8524-f69742cc4dad",
                        "/veo_client:76ca215f-f4e3-4cbd-8524-f69742cc4dae")
            }
        }

        shouldThrow<IllegalArgumentException> { sut.getClientId(auth) }
    }
}
