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

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import jakarta.validation.constraints.Max
import org.springframework.security.core.Authentication
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import org.veo.history.dtos.RevisionDto
import org.veo.history.dtos.RevisionPageDto
import org.veo.history.exceptions.ResourceNotFoundException
import java.net.URI
import java.time.Instant
import java.util.UUID

/**
 * Provides read-only endpoints for fetching resource revisions. Only revisions that belong the authenticated user's
 * client can be accessed.
 */
@RestController
@RequestMapping("/revisions")
@SecurityRequirement(name = VeoHistoryApplication.SECURITY_SCHEME_OAUTH)
@Validated
class RevisionController(
    private val repo: RevisionRepo,
    private val mapper: RevisionDtoFactory,
    private val authService: AuthService,
    private val domainService: DomainService,
) {
    @Operation(description = "Retrieve all revisions of the resource at given URI.")
    @GetMapping
    fun getRevisions(
        auth: Authentication,
        @RequestParam("uri") uri: URI,
    ): List<RevisionDto> {
        val domainSpecificResource = domainService.tryParseDomainSpecificUri(uri)
        if (domainSpecificResource != null) {
            return getRevisions(auth, domainSpecificResource.mainUri).map(domainSpecificResource::convert)
        }
        return repo.findAll(uri, authService.getClientId(auth)).map {
            mapper.createDto(it)
        }
    }

    @Operation(description = "Retrieve a revision with given change number.")
    @GetMapping("/change/{changeNumber}")
    fun getRevision(
        auth: Authentication,
        @RequestParam("uri") uri: URI,
        @PathVariable("changeNumber") changeNumber: Long,
    ): RevisionDto {
        val domainSpecificResource = domainService.tryParseDomainSpecificUri(uri)
        if (domainSpecificResource != null) {
            return domainSpecificResource.convert(getRevision(auth, domainSpecificResource.mainUri, changeNumber))
        }
        return repo.find(uri, changeNumber, authService.getClientId(auth))?.let {
            mapper.createDto(it)
        } ?: throw ResourceNotFoundException()
    }

    @Operation(description = "Retrieve the revision that was most recent at given point in time.")
    @GetMapping("/contemporary/{time}")
    fun getRevision(
        auth: Authentication,
        @RequestParam("uri") uri: URI,
        @PathVariable("time") time: Instant,
    ): RevisionDto {
        val domainSpecificResource = domainService.tryParseDomainSpecificUri(uri)
        if (domainSpecificResource != null) {
            return domainSpecificResource.convert(getRevision(auth, domainSpecificResource.mainUri, time))
        }
        return repo.find(uri, time, authService.getClientId(auth))?.let {
            mapper.createDto(it)
        } ?: throw ResourceNotFoundException()
    }

    @Operation(description = "Retrieve latest revision for each of the 10 resources most recently changed by the authenticated user.")
    @GetMapping("/my-latest")
    fun getMostRecentlyChangedResources(
        auth: Authentication,
        @RequestParam("owner") ownerTargetUri: URI,
    ): List<RevisionDto> =
        repo
            .findMostRecentlyChangedResources(authService.getUsername(auth), ownerTargetUri, authService.getClientId(auth))
            .map { mapper.createDto(it) }

    @Operation(description = "Retrieve all revisions using seek pagination")
    @GetMapping("/paged")
    fun getPaged(
        auth: Authentication,
        @RequestParam("size", defaultValue = "20")
        @Max(value = 10000)
        size: Int,
        @RequestParam("afterId") afterId: UUID?,
    ): RevisionPageDto =
        repo
            .findAll(size, afterId, authService.getClientId(auth))
            .let(mapper::createPageDto)
}
