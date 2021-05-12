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

import org.springframework.stereotype.Component
import org.veo.history.dtos.RevisionDto

@Component
class RevisionMapper {
    fun toDto(entity: Revision): RevisionDto {
        return RevisionDto(entity.changeNumber, entity.type, entity.time, entity.author, entity.content)
    }
}
