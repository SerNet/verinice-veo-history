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

import com.fasterxml.jackson.databind.JsonNode
import com.vladmihalcea.hibernate.type.json.JsonType
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.GeneratedValue
import jakarta.persistence.Id
import jakarta.persistence.Table
import jakarta.persistence.UniqueConstraint
import org.hibernate.annotations.Type
import java.net.URI
import java.time.Instant
import java.util.UUID
import java.util.UUID.randomUUID

/**
 * An archived revision of a veo REST resource.
 */
@Entity
@Table(uniqueConstraints = [UniqueConstraint(columnNames = arrayOf("uri", "changeNumber"))])
class Revision(
    /** Resource Location */
    val uri: URI,
    /** Type of change (what happened?) */
    @Enumerated(EnumType.STRING)
    val type: RevisionType,
    /** Resource-specific zero-based change number */
    val changeNumber: Long,
    /** Time of change. */
    val time: Instant,
    /** Username (who made the change?) */
    val author: String,
    /** ID of the client the resource belonged to. Other clients must never access this revision. */
    val clientId: UUID,
    /** Resource content (JSON response body at time of change). */
    @Type(JsonType::class)
    @Column(columnDefinition = "jsonb", name = "content")
    private val _content: JsonNode,
) {
    @Id
    @Column(name = "id")
    @GeneratedValue
    private var _id: Long = 0

    /** Incremental ID for internal use. Should NOT be exposed on the public API. */
    val id: Long get() = _id

    /** Unique key to be used on the public API to reference revisions. */
    val uuid: UUID = randomUUID()

    val content: JsonNode
        get() = _content.deepCopy() // enforce immutability
}
