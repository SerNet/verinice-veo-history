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
package org.veo.history.dtos

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.databind.JsonNode
import io.swagger.v3.oas.annotations.media.Schema
import org.veo.history.RevisionType
import java.net.URI
import java.time.Instant
import java.util.UUID

@Schema(
    description =
        "An archived revision of a resource from any of the veo REST services. Revisions are created when the resource is " +
            "created, modified or deleted.",
)
class RevisionDto(
    @Schema(description = "Unique revision identifier")
    val id: UUID,
    @Schema(description = "Location of the resource.")
    val uri: URI,
    @Schema(description = "Resource-specific zero-based change number.")
    val changeNumber: Long,
    @Schema(description = "Type of change that was performed.")
    val type: RevisionType,
    @Schema(description = "Time when this change was performed.")
    val time: Instant,
    @Schema(description = "Name of the user who authored the change.")
    val author: String,
    @Schema(description = "Resource body at this revision")
    @JsonIgnore
    private val _content: JsonNode?,
) {
    val content: JsonNode?
        get() = _content?.deepCopy() // enforce immutability
}
