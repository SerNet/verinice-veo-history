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
import java.io.ByteArrayInputStream
import java.io.ObjectInputStream
import java.net.URI

@SuppressWarnings("ClassName")
class V3__uri_to_varchar : BaseJavaMigration() {
    override fun migrate(context: Context) {
        context.connection.createStatement().use {
            it.execute(
                """
                ALTER TABLE revision DROP constraint UK_uri_change_number;
                ALTER TABLE revision ADD COLUMN temp_uri varchar(255);
                """,
            )
        }
        context.connection.createStatement().use {
            it.executeQuery("SELECT id,uri FROM revision;").use { rows ->
                while (rows.next()) {
                    val id = rows.getInt(1)
                    val uri = deserialize(rows.getBytes(2)) as URI
                    context.connection.createStatement().use { update ->
                        update.execute("UPDATE revision SET temp_uri = '$uri' WHERE id = $id;")
                    }
                }
            }
        }
        context.connection.createStatement().use {
            it.execute(
                """
                ALTER TABLE revision DROP COLUMN uri;
                ALTER TABLE revision RENAME COLUMN temp_uri TO uri; 
                ALTER TABLE revision ALTER COLUMN uri SET NOT NULL;
                ALTER TABLE revision ADD CONSTRAINT UK_uri_change_number unique (uri, change_number);
                """,
            )
        }
    }

    private fun deserialize(bytes: ByteArray): Any {
        ObjectInputStream(ByteArrayInputStream(bytes)).use {
            return it.readObject()
        }
    }
}
