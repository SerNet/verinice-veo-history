/**
 * verinice.veo history
 * Copyright (C) 2023  Jonas Jordan
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
import java.util.UUID.randomUUID

class V6__add_uuid_column : BaseJavaMigration() {
    override fun migrate(context: Context) {
        context.connection.createStatement().use {
            it.execute("alter table revision add column uuid uuid;")
        }
        val count = context.connection.createStatement().use {
            it.executeQuery("select count(id) from revision;").use { row ->
                row.next()
                row.getInt(1)
            }
        }
        // Split revisions into chunks and execute one long update statement per chunk to assign UUIDs.
        (1..count)
            .chunked(1000)
            .forEach { ids ->
                ids
                    .joinToString("") { id -> "update revision set uuid = '${randomUUID()}' where id = $id;" }
                    .let { sql -> context.connection.createStatement().use { it.execute(sql) } }
            }

        context.connection.createStatement().use {
            it.execute(
                """
                alter table revision alter column uuid set not null;
                alter table revision add constraint revision_uuid unique (uuid); 
                """,
            )
        }
    }
}
