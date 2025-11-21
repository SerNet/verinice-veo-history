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

import org.hibernate.type.descriptor.WrapperOptions
import org.hibernate.type.descriptor.java.JavaType
import org.hibernate.type.format.AbstractJsonFormatMapper
import tools.jackson.core.JsonGenerator
import tools.jackson.core.JsonParser
import tools.jackson.databind.json.JsonMapper
import java.io.IOException
import java.lang.reflect.Type

// TODO #4549: remove once we have a Hibernate version with Jackson 3 support
class JacksonJsonFormatMapper
    @JvmOverloads
    constructor(
        private val jsonMapper: JsonMapper = JsonMapper.shared(),
    ) : AbstractJsonFormatMapper() {
        @Throws(IOException::class)
        override fun <T> writeToTarget(
            value: T?,
            javaType: JavaType<T?>,
            target: Any?,
            options: WrapperOptions?,
        ) {
            this.jsonMapper
                .writerFor(this.jsonMapper.constructType(javaType.javaType))
                .writeValue(target as JsonGenerator?, value)
        }

        @Throws(IOException::class)
        override fun <T> readFromSource(
            javaType: JavaType<T?>,
            source: Any?,
            options: WrapperOptions?,
        ): T? =
            this.jsonMapper.readValue<T>(
                source as JsonParser?,
                this.jsonMapper.constructType(javaType.javaType),
            )

        override fun supportsSourceType(sourceType: Class<*>): Boolean = JsonParser::class.java.isAssignableFrom(sourceType)

        override fun supportsTargetType(targetType: Class<*>): Boolean = JsonGenerator::class.java.isAssignableFrom(targetType)

        public override fun <T> fromString(
            charSequence: CharSequence,
            type: Type?,
        ): T = this.jsonMapper.readValue<T>(charSequence.toString(), this.jsonMapper.constructType(type))

        public override fun <T> toString(
            value: T?,
            type: Type?,
        ): String? = this.jsonMapper.writerFor(this.jsonMapper.constructType(type)).writeValueAsString(value)
    }
