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

class Pick : AbstractCommand("pick", CommandCategory.UTIL, "Helps you choose an option. Supports more than 2 options.", "pick option1/option2/optionX") {
    override fun run(args: MutableList<String>, event: GuildMessageReceivedEvent) {
        val options = args
            .joinToString(" ")
            .split("/")
            .map { it.trimIndent() }
            .filter { it != "" }

        if (options.count() < 2) {
            event.channel.sendMessage("You need to provide me with at least 2 options!").queue()
            return
        }

        event.channel.sendMessage("${event.author.asMention}, I choose ${options.random()}!").queue()
    }
}
