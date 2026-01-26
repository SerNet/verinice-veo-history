/*
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

import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.context.SecurityContext
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken
import org.springframework.security.test.context.support.WithSecurityContextFactory
import java.time.Instant

class WithMockClientSecurityContextFactory : WithSecurityContextFactory<WithMockClient> {
    override fun createSecurityContext(annotation: WithMockClient): SecurityContext =
        SecurityContextHolder.createEmptyContext().apply {
            authentication =
                MockToken(
                    Jwt(
                        "test",
                        Instant.now(),
                        Instant.MAX,
                        mapOf("test" to "test"),
                        mapOf(
                            "preferred_username" to annotation.username,
                            "groups" to "/veo_client:$MOCK_CLIENT_UUID",
                        ),
                    ),
                    listOf("veo-user") + if (annotation.readWriteAllUnits) listOf("read_write_all_units") else emptyList(),
                )
        }

    class MockToken(
        jwt: Jwt,
        val roles: List<String>,
    ) : JwtAuthenticationToken(jwt) {
        override fun getAuthorities() = roles.map { r -> SimpleGrantedAuthority("ROLE_$r") }.toMutableList()

        override fun isAuthenticated(): Boolean = true
    }
}
