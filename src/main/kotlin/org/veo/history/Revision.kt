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

import java.net.URL
import java.time.Instant
import java.util.UUID

/**
 * An archived revision of a veo REST resource.
 */
class Revision(
    /** Resource Location */
    val url: URL,
    /** Type of change (what happened?) */
    val type: RevisionType,
    /** Incremental version number */
    val version: Int,
    /** Time of change. */
    val time: Instant,
    /** Username (who made the change?) */
    val author: String,
    /** ID of the client the resource belonged to. Other clients must never access this revision. */
    val clientId: UUID,
    /** Resource content (JSON response body at time of change). */
    val content: Any?
)
