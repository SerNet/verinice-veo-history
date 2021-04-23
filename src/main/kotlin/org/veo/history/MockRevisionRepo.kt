/**
 * Copyright (c) 2021 Jonas Jordan.
 *
 * This program is free software: you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public License
 * as published by the Free Software Foundation, either version 3
 * of the License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.
 * If not, see <http://www.gnu.org/licenses/>.
 */
package org.veo.history

import java.net.URI
import java.time.Instant
import java.util.UUID
import org.springframework.stereotype.Component

@Component
class MockRevisionRepo {
    private val clientId = UUID.randomUUID()
    private val revisions = mutableListOf(
        Revision(
            URI.create("/processes/85773f48-d7bb-4605-a0fe-9db6f1db5b82"),
            RevisionType.CREATION, 1,
            Instant.parse("2021-01-27T11:27:00.013621Z"), "dm", clientId, mapOf(
            "name" to "Process 1",
            "abbreviation" to "PS 1",
            "description" to "Lorem ipsum", "createdAt" to "2021-01-27T11:27:00.013621Z", "createdBy" to "dm",
            "updatedAt" to "2021-01-27T11:27:00.013621Z", "updatedBy" to "dm", "domains" to listOf(
            mapOf("displayName" to "Placeholder domain - see issue VEO-227", "searchesUri" to "",
                "targetUri" to "/domains/7c087c77-6d05-4839-a819-533aaeffdc4c")),
            "owner" to mapOf("displayName" to "SerNet Berlin",
                "resourcesUri" to "https://veo-web.develop.verinice.com/unitsmapOf(parent,displayName)",
                "searchesUri" to "https://veo-web.develop.verinice.com/units/searches",
                "targetUri" to "https://veo-web.develop.verinice.com/units/bcbddcde-cb35-4e82-83d1-dcd7e313748e"),
            "links" to emptyMap<String, Any>(),
            "customAspects" to emptyMap<String, Any>(),
            "subType" to mapOf("7c087c77-6d05-4839-a819-533aaeffdc4c" to "VT"), "parts" to emptyList<Any>(),
            "id" to "85773f48-d7bb-4605-a0fe-9db6f1db5b82")),
        Revision(
            URI.create("/processes/85773f48-d7bb-4605-a0fe-9db6f1db5b82"),
            RevisionType.MODIFICATION, 2,
            Instant.parse("2021-01-28T11:27:00.013621Z"), "jj", clientId, mapOf(
            "name" to "Super Process 1",
            "abbreviation" to "PS 1",
            "description" to "Lorem ipsum bipsum", "createdAt" to "2021-01-27T11:27:00.013621Z",
            "createdBy" to "dm",
            "updatedAt" to "2021-01-28T11:27:00.013621Z", "updatedBy" to "jj", "domains" to listOf(
            mapOf("displayName" to "Placeholder domain - see issue VEO-227", "searchesUri" to "",
                "targetUri" to "/domains/7c087c77-6d05-4839-a819-533aaeffdc4c")),
            "owner" to mapOf("displayName" to "SerNet Berlin",
                "resourcesUri" to "https://veo-web.develop.verinice.com/unitsmapOf(parent,displayName)",
                "searchesUri" to "https://veo-web.develop.verinice.com/units/searches",
                "targetUri" to "https://veo-web.develop.verinice.com/units/bcbddcde-cb35-4e82-83d1-dcd7e313748e"),
            "links" to emptyMap<String, Any>(),
            "customAspects" to emptyMap<String, Any>(),
            "subType" to mapOf("7c087c77-6d05-4839-a819-533aaeffdc4c" to "VT"), "parts" to emptyList<Any>(),
            "id" to "85773f48-d7bb-4605-a0fe-9db6f1db5b82")),
        Revision(
            URI.create("/processes/85773f48-d7bb-4605-a0fe-9db6f1db5b82"),
            RevisionType.MODIFICATION, 3,
            Instant.parse("2021-01-29T11:27:00.013621Z"), "jj", clientId, mapOf(
            "name" to "Super Process 1",
            "abbreviation" to "SPS 1",
            "description" to "Lorem ipsum bipsum", "createdAt" to "2021-01-27T11:27:00.013621Z",
            "createdBy" to "dm",
            "updatedAt" to "2021-01-29T11:27:00.013621Z", "updatedBy" to "jj", "domains" to listOf(
            mapOf("displayName" to "Placeholder domain - see issue VEO-227", "searchesUri" to "",
                "targetUri" to "/domains/7c087c77-6d05-4839-a819-533aaeffdc4c")),
            "owner" to mapOf("displayName" to "SerNet Berlin",
                "resourcesUri" to "https://veo-web.develop.verinice.com/unitsmapOf(parent,displayName)",
                "searchesUri" to "https://veo-web.develop.verinice.com/units/searches",
                "targetUri" to "https://veo-web.develop.verinice.com/units/bcbddcde-cb35-4e82-83d1-dcd7e313748e"),
            "links" to emptyMap<String, Any>(),
            "customAspects" to emptyMap<String, Any>(),
            "subType" to mapOf("7c087c77-6d05-4839-a819-533aaeffdc4c" to "VT"), "parts" to emptyList<Any>(),
            "id" to "85773f48-d7bb-4605-a0fe-9db6f1db5b82")),
        Revision(
            URI.create("/processes/85773f48-d7bb-4605-a0fe-9db6f1db5b82"),
            RevisionType.MODIFICATION, 4,
            Instant.parse("2021-01-30T11:27:00.013621Z"), "jk", clientId, mapOf(
            "name" to "Super Process 1",
            "abbreviation" to "SPS 1",
            "description" to "Lorem ipsum bipsum", "createdAt" to "2021-01-27T11:27:00.013621Z",
            "createdBy" to "dm",
            "updatedAt" to "2021-01-29T11:27:00.013621Z", "updatedBy" to "jk", "domains" to listOf(
            mapOf("displayName" to "Placeholder domain - see issue VEO-227", "searchesUri" to "",
                "targetUri" to "/domains/7c087c77-6d05-4839-a819-533aaeffdc4c")),
            "owner" to mapOf("displayName" to "SerNet Berlin",
                "resourcesUri" to "https://veo-web.develop.verinice.com/unitsmapOf(parent,displayName)",
                "searchesUri" to "https://veo-web.develop.verinice.com/units/searches",
                "targetUri" to "https://veo-web.develop.verinice.com/units/bcbddcde-cb35-4e82-83d1-dcd7e313748e"),
            "links" to emptyMap<String, Any>(),
            "customAspects" to emptyMap<String, Any>(),
            "subType" to emptyMap<String, Any>(), "parts" to emptyList<Any>(),
            "id" to "85773f48-d7bb-4605-a0fe-9db6f1db5b82")),
        Revision(
            URI.create("/processes/85773f48-d7bb-4605-a0fe-9db6f1db5b82"),
            RevisionType.HARD_DELETION, 5,
            Instant.parse("2021-01-30T11:27:00.013621Z"), "dm", clientId, null)
    )

    fun findAll(uri: URI): List<Revision> = revisions

    fun find(uri: URI, version: Long): Revision? = revisions.firstOrNull { it.version == version }

    fun find(uri: URI, time: Instant): Revision? =
        revisions.sortedByDescending { it.time }.firstOrNull { it.time <= time }

    @Throws(DuplicateRevisionException::class)
    fun add(revision: Revision) {
        if (revisions.any { it.uri == revision.uri && it.version == revision.version }) {
            throw DuplicateRevisionException(revision.uri, revision.version)
        }
        revisions.add(revision)
    }
}
