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

import org.springframework.stereotype.Component
import org.veo.history.dtos.RevisionDto
import org.veo.history.dtos.RevisionPageDto
import org.veo.history.jpa.RevisionPage

@Component
class RevisionDtoFactory {
    fun createDto(entity: Revision): RevisionDto =
        RevisionDto(entity.uuid, entity.uri, entity.changeNumber, entity.type, entity.time, entity.author, entity.content)

    fun createPageDto(page: RevisionPage): RevisionPageDto =
        page.run {
            RevisionPageDto(
                totalItemCount,
                items.map(::createDto),
            )
        }
}
