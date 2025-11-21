/*
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
package org.veo.history

import org.springframework.stereotype.Component
import org.veo.history.dtos.RevisionDto
import tools.jackson.databind.node.ObjectNode
import java.net.URI
import java.util.UUID

/**
 * Some resources in veo have domain-specific representations (e.g. /assets/{assetUuid} is also available under
 * /domains/{domainId}/assets/{assetUuid}). The history DB only stores revisions for the main representations of resources, and not the
 * domain-specific variants.
 * This class allows recognizing and parsing domain-specific URIs, and converting revisions into a domain-specific format, so a resource's
 * history can be consumed from the viewpoint of a domain.
 */
@Component
class DomainService {
    private val elementTypes = listOf("assets", "controls", "document", "incidents", "persons", "processes", "scenarios", "scopes")
    private val pattern = Regex("/domains/($UUID_PATTERN)(/(${elementTypes.joinToString("|")})/$UUID_PATTERN)")

    fun tryParseDomainSpecificUri(uri: URI): DomainSpecificResource? =
        pattern
            .matchEntire(uri.toString())
            ?.groupValues
            ?.let { DomainSpecificResource(uri, URI.create(it[2]), UUID.fromString(it[1])) }

    data class DomainSpecificResource(
        val domainSpecificUri: URI,
        val mainUri: URI,
        val domainId: UUID,
    ) {
        /**
         * Converts given revision into a domain-specific representation. URI and content are modified.
         */
        fun convert(revisionDto: RevisionDto): RevisionDto =
            RevisionDto(
                revisionDto.id,
                domainSpecificUri,
                revisionDto.changeNumber,
                revisionDto.type,
                revisionDto.time,
                revisionDto.author,
                revisionDto.content
                    ?.let { it as ObjectNode }
                    ?.also { content ->
                        content.remove("requirementImplementations")
                        content
                            .remove("domains")
                            ?.get(domainId.toString())
                            ?.let { it as ObjectNode }
                            ?.let { domainAssociation ->
                                content.setAll(domainAssociation)
                            }
                    },
            )
    }
}
