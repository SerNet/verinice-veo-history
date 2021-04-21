/**
 * Copyright (c) 2021 Jonas Jordan.
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
package org.veo.history

import io.kotest.assertions.throwables.shouldThrow
import java.net.URI
import java.time.Instant
import java.util.UUID
import org.junit.jupiter.api.Test

class RevisionRepoTest {
    private val sut = RevisionRepo()

    @Test
    fun `throws when adding duplicate revisions`() {
        // Adding different versions of the same resource is OK
        sut.add(
            Revision(URI.create("/foo/bar"), RevisionType.MODIFICATION, 12, Instant.now(), "nobody", UUID.randomUUID(),
                emptyMap<String, String>()))
        sut.add(
            Revision(URI.create("/foo/bar"), RevisionType.MODIFICATION, 13, Instant.now(), "anybody", UUID.randomUUID(),
                emptyMap<String, String>()))

        // Adding the same version number of a different resource is also OK.
        sut.add(
            Revision(URI.create("/foo/car"), RevisionType.MODIFICATION, 13, Instant.now(), "anybody", UUID.randomUUID(),
                emptyMap<String, String>()))

        // Adding the same version number of the same resource should cause an exception.
        shouldThrow<DuplicateRevisionException> {
            sut.add(
                Revision(URI.create("/foo/bar"), RevisionType.MODIFICATION, 13, Instant.now(), "anybody else",
                    UUID.randomUUID(),
                    emptyMap<String, String>()))
        }
    }
}
