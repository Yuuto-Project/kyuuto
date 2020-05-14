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

import io.github.yuutoproject.yuutobot.Utils
import net.dv8tion.jda.api.utils.data.DataObject

data class Character(val characterObject: DataObject) {
    val name = characterObject.getString("name")
    val description = characterObject.getString("description")
    val age = characterObject.getString("age")
    val birthday = characterObject.getString("birthday")
    val animal = characterObject.getString("animal")
    val color = Utils.hexStringToInt(characterObject.getString("color"))
    val emoteId = characterObject.getString("emoteId")
}
