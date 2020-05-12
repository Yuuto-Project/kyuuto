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

class Ping : AbstractCommand(
    "ping",
    CommandCategory.INFO,
    "Shows the ping",
    "Run `ping` to get the current latency and API ping."
) {
    private val pings = arrayOf("Ping", "Pong", "Pang", "Peng")

    override fun run(args: MutableList<String>, event: GuildMessageReceivedEvent) {
        event.jda.restPing.queue { ping ->
            // A lot of entities in JDA return a RestAction
            // A rest action can do lost of cool things
            // But in most cases we just queue it off
            // It is important to use queue instead of complete as we don't want to block the event thread
            // If we block the event thread JDA cannot receive messages anymore
            event.channel.sendMessage("${pings.random()}! Ping is $ping ms").queue()
        }
    }
}
