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

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.ObjectNode
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test
import java.net.URI
import java.time.Instant
import java.util.UUID

class RevisionTest {
    val om = ObjectMapper()

    @Test
    fun `content is immutable`() {
        // given a revision
        val revision =
            Revision(
                URI.create("verinice.com"),
                RevisionType.CREATION,
                44,
                Instant.now(),
                "me",
                UUID.randomUUID(),
                om.createObjectNode().put("foo", "bar"),
            )

        // when trying to mutate its content
        (revision.content as ObjectNode).put("woo", "star")

        // then nothing has changed
        revision.content!!.get("woo") shouldBe null
    }
}
