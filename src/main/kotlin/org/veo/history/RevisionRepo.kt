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
import java.util.UUID
import org.springframework.stereotype.Component

@Component
class RevisionRepo {
    private val revisions = mutableListOf<Revision>()

    fun findAll(uri: URI, clientId: UUID): List<Revision> = revisions
        .filter { it.clientId == clientId && it.uri == uri }

    fun find(uri: URI, changeNumber: Long, clientId: UUID): Revision? =
        findAll(uri, clientId).firstOrNull { it.changeNumber == changeNumber }

    fun find(uri: URI, time: Instant, clientId: UUID): Revision? =
        findAll(uri, clientId).sortedByDescending { it.time }.firstOrNull { it.time <= time }

    @Throws(DuplicateRevisionException::class)
    fun add(revision: Revision) {
        if (revisions.any { it.uri == revision.uri && it.changeNumber == revision.changeNumber }) {
            throw DuplicateRevisionException(revision.uri, revision.changeNumber)
        }
        revisions.add(revision)
    }

    fun clear() = revisions.clear()
}
