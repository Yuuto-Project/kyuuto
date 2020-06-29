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
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent

class Enlarge : AbstractCommand("enlarge", CommandCategory.UTIL, "Returns an enlarged emote", "Run `enlarge <emote>` to get the full link to `<emote>` at a large size") {
    override fun run(args: MutableList<String>, event: GuildMessageReceivedEvent) {
        val emotes = event.message.emotes

        if (emotes.isEmpty()) {
            event.channel.sendMessage("Sorry, but you need to provide me an emote to use this command~!").queue()
            return
        }

        val emoteLink = emotes.first().imageUrl

        event.channel.sendMessage("${event.author.asMention}, here you go~!\n$emoteLink").queue()
    }
}
