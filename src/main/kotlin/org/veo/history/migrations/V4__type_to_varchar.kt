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

class V4__type_to_varchar : BaseJavaMigration() {
    override fun migrate(context: Context) {
        context.connection.createStatement().use {
            it.execute(
                """
                ALTER TABLE revision ADD COLUMN temp_type varchar(255);    
                UPDATE revision SET temp_type = 'CREATION' WHERE type = 0;
                UPDATE revision SET temp_type = 'MODIFICATION' WHERE type = 1;
                UPDATE revision SET temp_type = 'HARD_DELETION' WHERE type = 2;
                ALTER TABLE revision DROP COLUMN type;
                ALTER TABLE revision RENAME COLUMN temp_type TO type;
                ALTER TABLE revision ALTER COLUMN type SET NOT NULL;
                """
            )
        }
    }
}
