/**
 * verinice.veo reporting
 * Copyright (C) 2020  Alexander Nasrallah
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
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.arbitrary
import io.kotest.property.arbitrary.list
import io.kotest.property.arbitrary.string
import io.kotest.property.checkAll
import io.mockk.every
import io.mockk.mockk
import java.util.UUID
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken

class AuthServiceUnitProp : StringSpec({
    val sut = AuthService()

    val uuidArb = arbitrary { rs ->
        UUID(rs.random.nextLong(), rs.random.nextLong())
    }

    "parses client UUID" {
        checkAll(uuidArb) { uuid ->
            val auth = mockk<JwtAuthenticationToken> {
                every { token } returns mockk {
                    every { getClaimAsStringList("groups") } returns listOf("/veo_client:$uuid")
                }
            }
            val clientId = sut.getClientId(auth)
            uuid shouldBe clientId
        }
    }

    "parses client UUID with mixed groups" {
        checkAll(uuidArb, Arb.list(Arb.string(), 1..30)) { uuid, strs ->
            // make sure the group doesn't start with /veo_client:
            val groupNames = strs.map { "x$it" }
            val auth = mockk<JwtAuthenticationToken> {
                every { token } returns mockk {
                    every { getClaimAsStringList("groups") } returns groupNames + ("/veo_client:$uuid")
                }
            }
            val clientId = sut.getClientId(auth)
            uuid shouldBe clientId
        }
    }

    "throws exception for multiple group claims" {
        checkAll(Arb.list(uuidArb, 2..30)) { uuids ->
            val auth = mockk<JwtAuthenticationToken> {
                every { token } returns mockk {
                    every { getClaimAsStringList("groups") } returns (uuids.map { "/veo_client:$it" })
                }
            }
            shouldThrow<IllegalArgumentException> { sut.getClientId(auth) }
        }
    }
})
