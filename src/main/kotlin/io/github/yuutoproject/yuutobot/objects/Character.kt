/*
 * Open source bot built by and for the Camp Buddy Discord Fan Server.
 *     Copyright (C) 2020  Kyuuto-devs
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package io.github.yuutoproject.yuutobot.objects

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty
import io.github.yuutoproject.yuutobot.Utils

class Character @JsonCreator constructor(
    @JsonProperty("name") val name: String,
    @JsonProperty("description") val description: String,
    @JsonProperty("age") val age: String,
    @JsonProperty("birthday") val birthday: String,
    @JsonProperty("animal") val animal: String,
    @JsonProperty("color") private val colorString: String,
    @JsonProperty("emoteId") val emoteId: String
) {
    val color = Utils.hexStringToInt(colorString)
    val firstName = name.split(" ")[0]
}
