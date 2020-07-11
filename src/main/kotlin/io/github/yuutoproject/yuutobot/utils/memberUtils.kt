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

package io.github.yuutoproject.yuutobot.utils

import com.jagrosh.jdautilities.commons.utils.FinderUtil
import net.dv8tion.jda.api.entities.Member
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent

fun findMember(input: String, event: GuildMessageReceivedEvent): Member? {
    val foundMembers = FinderUtil.findMembers(input, event.guild)

    if (foundMembers.isEmpty()) {
        return null
    }

    // why not "?: null"
    // Well java is fun and will throw an index out of bounds exception if there is no first element
    return foundMembers[0]
}
