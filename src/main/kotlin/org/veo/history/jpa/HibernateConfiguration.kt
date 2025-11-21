/*
 * verinice.veo history
 * Copyright (C) 2026  Jochen Kemnade
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
package org.veo.history.jpa

import org.hibernate.cfg.AvailableSettings
import org.springframework.boot.hibernate.autoconfigure.HibernatePropertiesCustomizer
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

// TODO #4549: remove once we have a Hibernate version with Jackson 3 support
@Configuration
class HibernateConfiguration {
    @Bean
    fun hibernatePropertiesCustomizer(): HibernatePropertiesCustomizer =
        HibernatePropertiesCustomizer { hibernateProperties: MutableMap<String, Any> ->
            hibernateProperties[AvailableSettings.JSON_FORMAT_MAPPER] = JacksonJsonFormatMapper::class.java.getName()
        }
}
