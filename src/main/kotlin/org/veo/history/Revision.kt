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
// TODO VEO-972 Wait for hibernate 6.0, use new custom type API, remove suppressor
@file:Suppress("DEPRECATION")

package org.veo.history

import com.fasterxml.jackson.databind.JsonNode
import com.vladmihalcea.hibernate.type.json.JsonType
import org.hibernate.annotations.Type
import org.hibernate.annotations.TypeDef
import java.net.URI
import java.time.Instant
import java.util.UUID
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.Id
import javax.persistence.Table
import javax.persistence.UniqueConstraint

/**
 * An archived revision of a veo REST resource.
 */
@Entity
@Table(uniqueConstraints = [UniqueConstraint(columnNames = arrayOf("uri", "changeNumber"))])
@TypeDef(name = "json", typeClass = JsonType::class)
class Revision(
    /** Resource Location */
    val uri: URI,
    /** Type of change (what happened?) */
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
    @Type(type = "json")
    @Column(columnDefinition = "jsonb")
    val content: JsonNode
) {
    @Id
    @GeneratedValue
    private var id: Long = 0
}
