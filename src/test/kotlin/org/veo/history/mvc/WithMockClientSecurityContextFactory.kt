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

import java.time.Instant
import org.springframework.security.core.context.SecurityContext
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken
import org.springframework.security.test.context.support.WithSecurityContextFactory

class WithMockClientSecurityContextFactory : WithSecurityContextFactory<WithMockClient> {
    override fun createSecurityContext(annotation: WithMockClient): SecurityContext {
        return SecurityContextHolder.createEmptyContext().apply {
            authentication = MockToken(
                Jwt("test", Instant.now(), Instant.now(),
                    mapOf("test" to "test"), mapOf("groups" to "/veo_client:" + annotation.clientUuid)))
        }
    }

    class MockToken(jwt: Jwt) : JwtAuthenticationToken(jwt) {
        override fun isAuthenticated(): Boolean {
            return true
        }
    }
}
