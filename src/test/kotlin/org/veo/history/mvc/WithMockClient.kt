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

import org.springframework.security.test.context.support.WithSecurityContext

const val mockClientUuid = "21712604-ed85-4f08-aa46-1cf39607ee9e"

@Retention(AnnotationRetention.RUNTIME)
@WithSecurityContext(factory = WithMockClientSecurityContextFactory::class)
annotation class WithMockClient
