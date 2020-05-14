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

data class Character(val routeMap: HashMap<String, Any>) {
    val name = routeMap.getValue("name") as String
    val description = routeMap.getValue("description") as String
    val age = routeMap.getValue("age").toString()
    val birthday = routeMap.getValue("birthday") as String
    val animal = routeMap.getValue("animal") as String
    val color = Utils.hexStringToInt(routeMap.getValue("color") as String)
    val emoteId = routeMap.getValue("emoteId") as String
}
