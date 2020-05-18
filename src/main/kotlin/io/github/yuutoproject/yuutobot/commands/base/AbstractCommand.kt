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

package io.github.yuutoproject.yuutobot.commands.base

import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent

/**
 * Base class for all commands
 *
 * The following properties have to be filled
 * - [name]: The name of the command
 * - [category]: The category of the command
 * - [description]: Oneliner that shows in "y!help list"
 * - [parameters]: Short string of all the parameters the command accepts
 * - [notes]: Additional notes that the user may have to note
 *
 * Commands can add aliases by overriding the [aliases] prop
 */
abstract class AbstractCommand(
    val name: String,
    val category: CommandCategory,
    val description: String,
    val parameters: String,
    val notes: String
) {
    /**
     * List of aliases for the command, is empty by default
     */
    open val aliases = emptyArray<String>()

    abstract fun run(args: MutableList<String>, event: GuildMessageReceivedEvent)

    override fun toString(): String {
        return "AbstractCommand(name='$name', category=$category, description='$description', usage='$notes')"
    }
}
