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

import java.net.URI
import java.time.Instant
import org.springframework.stereotype.Component

@Component
class RevisionRepo {
    private val revisions = mutableListOf<Revision>()

    fun findAll(uri: URI): List<Revision> = revisions
        .filter { it.uri == uri }

    fun find(uri: URI, version: Long): Revision? = findAll(uri).firstOrNull { it.version == version }

    fun find(uri: URI, time: Instant): Revision? =
        findAll(uri).sortedByDescending { it.time }.firstOrNull { it.time <= time }

    @Throws(DuplicateRevisionException::class)
    fun add(revision: Revision) {
        if (revisions.any { it.uri == revision.uri && it.version == revision.version }) {
            throw DuplicateRevisionException(revision.uri, revision.version)
        }
        revisions.add(revision)
    }

    fun clear() = revisions.clear()
}
