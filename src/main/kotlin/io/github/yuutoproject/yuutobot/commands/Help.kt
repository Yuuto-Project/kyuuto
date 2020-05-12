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

import io.github.yuutoproject.yuutobot.Listener
import io.github.yuutoproject.yuutobot.commands.base.AbstractCommand
import io.github.yuutoproject.yuutobot.commands.base.CommandCategory
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent

class Help : AbstractCommand(
    "help",
    CommandCategory.INFO,
    "Get the usage of any other command",
    "Run `help <command>` to get usage instructions on `<command>`, if it exists. Run `help list` to list possible commands."
) {
    override val aliases = arrayOf("usage", "commands")

    override fun run(args: MutableList<String>, event: GuildMessageReceivedEvent) {
        // Unsatisfactory solution for now, requires rework of command registration & execution
        val listener = event.jda.registeredListeners[0] as Listener
        val commands = listener.commands
        val aliases = listener.aliases

        val commandName = args.getOrElse(0) { "list" }

        if (commandName == "list") {
            val groups = commands.values.groupBy { it.category }

            val list = groups.map { (category, categoryCommands) ->
                "$category\n" + categoryCommands.joinToString("") { "`${it.name}` - ${it.description}\n" }
            }

            event.channel.sendMessage(
                "Here is a list of all commands and their descriptions:\n\n" +
                    list.joinToString("\n")
            ).queue()
            return
        }


        // No need to worry about indexing with null, it just returns null
        val command = commands[commandName] ?: commands[aliases[commandName]] ?: run {
            event.channel.sendMessage("Sorry, that command does not exist...").queue()
            return
        }

        event.channel.sendMessage(
            "**Category:** ${command.category}\n" +
                "**Usage for command:** `${command.name}`\n\n" +
                command.usage +
                if (command.aliases.isNotEmpty()) "\n\n**Aliases:** `${command.aliases.joinToString("`, `")}`" else ""
        ).queue()
    }
}
