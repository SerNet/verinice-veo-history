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
package org.veo.history.jpa

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional
import org.veo.history.Revision
import java.net.URI
import java.time.Instant
import java.util.UUID

@Repository
@Transactional
interface RevisionJpaRepo : JpaRepository<Revision, Long> {
    @Query("SELECT e FROM Revision e WHERE uri = :uri AND clientId = :clientId ORDER BY changeNumber")
    fun findAll(
        uri: URI,
        clientId: UUID,
    ): List<Revision>

    @Query("SELECT e FROM Revision e WHERE uri = :uri AND changeNumber = :changeNumber AND clientId = :clientId")
    fun find(
        uri: URI,
        changeNumber: Long,
        clientId: UUID,
    ): Revision?

    @Query(
        "SELECT * FROM revision WHERE uri = :uri AND time <= :time AND client_id = :clientId ORDER BY time DESC  LIMIT 1",
        nativeQuery = true,
    )
    fun find(
        uri: String,
        time: Instant,
        clientId: UUID,
    ): Revision?

    /** JSON query example */
    @Query("SELECT * FROM revision WHERE content ->> 'name' = :name AND client_id = :clientId", nativeQuery = true)
    fun find(
        name: String,
        clientId: UUID,
    ): List<Revision>

    /**
     * Returns the latest revision for each of the 10 most recently revised resources (excluding resources that have
     * been deleted).
     */
    @Query(
        """
        WITH latestByResource AS (
            SELECT distinct on (uri) * FROM revision 
            WHERE client_id = :clientId AND author = :author AND content -> 'owner' ->> 'targetUri' = :ownerTargetUri 
            ORDER by uri, change_number DESC
        )

        SELECT * FROM latestByResource WHERE type != 'HARD_DELETION' ORDER BY latestByResource.time DESC LIMIT 10;
        """,
        nativeQuery = true,
    )
    fun findMostRecentlyChangedResources(
        author: String,
        ownerTargetUri: String,
        clientId: UUID,
    ): List<Revision>

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("delete from revision  r where r.client_id = :clientId", nativeQuery = true)
    fun deleteAllClientRevisions(clientId: UUID)

    @Query("select r from Revision r where r.uuid = :revisionUuid and r.clientId = :clientId")
    fun findByUuid(
        revisionUuid: UUID,
        clientId: UUID,
    ): Revision?
}
