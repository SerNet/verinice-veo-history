/**
 * verinice.veo reporting
 * Copyright (C) 2021  Jonas Jordan
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
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Test
import org.springframework.dao.DataIntegrityViolationException
import org.veo.history.jpa.RevisionJpaRepo

class RevisionRepoTest {
    private val jpaRepo = mockk<RevisionJpaRepo>()
    private val sut = RevisionRepo(jpaRepo)

    @Test
    fun `wraps data integrity violation`() {
        every { jpaRepo.save(any()) } throws DataIntegrityViolationException("")

        shouldThrow<DuplicateRevisionException> {
            sut.add(mockk(relaxed = true))
        }
    }
}
