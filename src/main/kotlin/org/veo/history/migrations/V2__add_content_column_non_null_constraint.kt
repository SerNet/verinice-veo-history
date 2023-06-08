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
package org.veo.history.migrations

import org.flywaydb.core.api.migration.BaseJavaMigration
import org.flywaydb.core.api.migration.Context

@SuppressWarnings("ClassName")
class V2__add_content_column_non_null_constraint : BaseJavaMigration() {
    override fun migrate(context: Context) {
        context.connection.createStatement().use {
            it.execute(
                """
                UPDATE revision
                    SET content = lastNonDeletionRevision.content
                FROM (
                  SELECT distinct ON (uri) uri, content FROM revision WHERE type != 2 ORDER BY uri, change_number DESC
                ) AS lastNonDeletionRevision
                WHERE revision.type = 2 AND revision.uri = lastNonDeletionRevision.uri;
            
                alter table revision alter column content set not null;
                """,
            )
        }
    }
}
