/**
 * verinice.veo history
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

import jakarta.persistence.EntityManager
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.stereotype.Component
import org.veo.history.jpa.RevisionJpaRepo
import org.veo.history.jpa.RevisionPage
import java.net.URI
import java.time.Instant
import java.util.UUID

@Component
class RevisionRepo(
    private val jpaRepo: RevisionJpaRepo,
    private val entityManager: EntityManager,
) {
    fun findAll(uri: URI, clientId: UUID) = jpaRepo.findAll(uri, clientId)

    fun find(uri: URI, changeNumber: Long, clientId: UUID) = jpaRepo.find(
        uri,
        changeNumber,
        clientId,
    )

    fun find(uri: URI, time: Instant, clientId: UUID) = jpaRepo.find(uri.toString(), time, clientId)

    fun findMostRecentlyChangedResources(author: String, ownerTargetUri: URI, clientId: UUID) =
        jpaRepo.findMostRecentlyChangedResources(author, ownerTargetUri.toString(), clientId)

    @Throws(DuplicateRevisionException::class)
    fun add(revision: Revision) {
        try {
            jpaRepo.save(revision)
        } catch (ex: DataIntegrityViolationException) {
            throw DuplicateRevisionException(revision.uri, revision.changeNumber, ex)
        }
    }

    fun clear() = jpaRepo.deleteAll()
    fun deleteAllClientRevisions(clientId: UUID) = jpaRepo.deleteAllClientRevisions(clientId)
    fun findAll(size: Int, afterUuid: UUID?, clientId: UUID): RevisionPage {
        val afterRevision = afterUuid?.let { jpaRepo.getByUuid(it, clientId) }
        return RevisionPage(
            entityManager
                .createQuery("select count(r.id) from Revision r where r.clientId = :clientId")
                .setParameter("clientId", clientId)
                .singleResult as Long,
            entityManager
                .createQuery("select r from Revision r where r.clientId = :clientId and r.id > :afterId order by r.id")
                .setParameter("clientId", clientId)
                .setParameter("afterId", afterRevision?.id ?: -1)
                .setMaxResults(size)
                .resultList
                .map { it as Revision },
        )
    }
}
