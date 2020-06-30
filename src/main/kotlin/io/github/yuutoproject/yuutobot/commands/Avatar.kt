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

package io.github.yuutoproject.yuutobot.commands

import io.github.yuutoproject.yuutobot.commands.base.AbstractCommand
import io.github.yuutoproject.yuutobot.commands.base.CommandCategory
import io.github.yuutoproject.yuutobot.utils.findMember
import net.dv8tion.jda.api.entities.User
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent

class Avatar : AbstractCommand(
    "avatar",
    CommandCategory.UTIL,
    "Gets your own or someone's avatar",
    "avatar or avatar <user>"
) {
    override fun run(args: MutableList<String>, event: GuildMessageReceivedEvent) {
        var user: User? = event.author

        if (args.isNotEmpty()) {
            user = findMember(args.joinToString(" "), event)?.user
        }

        if (user == null) {
            event.channel.sendMessage("${event.author.asMention} Sorry, but I can't find that user").queue()
            return
        }

        event.channel.sendMessage("${event.author.asMention}, Here ya go~!\n" + user.effectiveAvatarUrl + "?size=2048").queue()
    }
}
