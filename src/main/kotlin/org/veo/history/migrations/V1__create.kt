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

class V1__create : BaseJavaMigration() {
    override fun migrate(context: Context) {
        context.connection.createStatement().use {
            it.execute(
                """
                create table revision (
                   id int8 not null,
                    author varchar(255),
                    change_number int8 not null,
                    client_id uuid,
                    content jsonb,
                    time timestamp,
                    type int4,
                    uri bytea,
                    primary key (id)
                );
            
                alter table revision 
                   add constraint UK_uri_change_number unique (uri, change_number);
            
                create sequence hibernate_sequence start 1 increment 1;
            
                CREATE INDEX revision_content_owner ON revision USING HASH((content -> 'owner' ->> 'targetUri'));
                """,
            )
        }
    }
}
