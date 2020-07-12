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

import io.github.yuutoproject.yuutobot.CommandManager
import io.github.yuutoproject.yuutobot.commands.base.AbstractCommand
import io.github.yuutoproject.yuutobot.commands.base.CommandCategory
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent

class Help(private val commandManager: CommandManager) : AbstractCommand(
    "help",
    CommandCategory.INFO,
    "Get the usage of any command",
    "[command]",
    "Run `help list` to list possible commands"
) {
    override val aliases = arrayOf("usage", "commands")

    override fun run(args: MutableList<String>, event: GuildMessageReceivedEvent) {
        val commands = this.commandManager.commands
        val aliases = this.commandManager.aliases

        val commandName = args.getOrElse(0) { "list" }

        if (commandName == "list") {
            val message = event.channel.sendMessage(
                "Here is a list of all commands and their descriptions:\n"
            )

            val groups = commands.values.groupBy(AbstractCommand::category)

            for ((category, commandsInCategory) in groups) {
                message.append("\n$category\n")

                for (command in commandsInCategory) {
                    message.append("`${command.name}` - ${command.description}\n")
                }
            }

            message.queue()
            return
        }

        // No need to worry about indexing with null, it just returns null
        val command = commands[commandName] ?: commands[aliases[commandName]] ?: run {
            event.channel.sendMessage("Sorry, that command does not exist...").queue()
            return
        }

        val message = event.channel.sendMessage(
            "**Category:** ${command.category}"
        )

        message.append(
            if (command.parameters.isNotBlank()) "\n**Usage:** `${commandManager.prefix}${command.name} ${command.parameters}`"
            else "\n**Usage:** `${commandManager.prefix}${command.name}`"
        )

        message.append(
            "\n**Description:** ${command.description.trim('.', '!')}. "
        )

        if (command.notes.isNotBlank()) {
            message.append("${command.notes.trim('.', '!')}.")
        }

        if (command.aliases.isNotEmpty()) {
            message.append("\n**Aliases:** `${command.aliases.joinToString("`, `")}`")
        }

        message.queue()
    }
}
