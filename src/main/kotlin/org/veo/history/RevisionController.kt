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
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import org.veo.history.dtos.RevisionDto
import org.veo.history.exceptions.ResourceNotFoundException
import java.net.URI
import java.time.Instant

/**
 * Provides read-only endpoints for fetching resource revisions. Only revisions that belong the authenticated user's
 * client can be accessed.
 */
@RestController
@RequestMapping("/revisions")
@SecurityRequirement(name = VeoHistoryApplication.SECURITY_SCHEME_OAUTH)
class RevisionController(
    private val repo: RevisionRepo,
    private val mapper: RevisionDtoFactory,
    private val authService: AuthService,
) {

    @Operation(description = "Retrieve all revisions of the resource at given URI.")
    @GetMapping
    fun getRevisions(auth: Authentication, @RequestParam("uri") uri: URI): List<RevisionDto> {
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
        return repo.find(uri, time, authService.getClientId(auth))?.let {
            mapper.createDto(it)
        } ?: throw ResourceNotFoundException()
    }

    @Operation(description = "Retrieve latest revision for each of the 10 resources most recently changed by the authenticated user.")
    @GetMapping("/my-latest")
    fun getMostRecentlyChangedResources(
        auth: Authentication,
        @RequestParam("owner") ownerTargetUri: URI,
    ): List<RevisionDto> {
        return repo.findMostRecentlyChangedResources(authService.getUsername(auth), ownerTargetUri, authService.getClientId(auth))
            .map { mapper.createDto(it) }
    }
}
