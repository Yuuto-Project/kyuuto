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

package io.github.yuutoproject.yuutobot

import io.github.yuutoproject.yuutobot.commands.Help
import io.github.yuutoproject.yuutobot.commands.base.Command
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent
import org.reflections.Reflections
import org.slf4j.LoggerFactory
import java.lang.reflect.Modifier

class CommandManager {
    private val logger = LoggerFactory.getLogger(this.javaClass)

    val commands = hashMapOf<String, Command>()
    val aliases = hashMapOf<String, String>()
    val prefix = Yuuto.config.get("PREFIX", "!")

    init {
        loadCommands()
    }

    private fun register(instance: Command) {
        commands[instance.name] = instance

        instance.aliases.forEach { alias ->
            aliases[alias] = instance.name
        }
    }

    private fun loadCommands() {
        register(Help(this))

        val reflections = Reflections("io.github.yuutoproject.yuutobot.commands")

        reflections.getSubTypesOf(Command::class.java)
            .filter { !Modifier.isAbstract(it.modifiers) && it.declaredConstructors[0].parameters.isEmpty() }
            .forEach {
                register(it.getDeclaredConstructor().newInstance())
            }
    }

    fun handleMessage(event: GuildMessageReceivedEvent) {
        val author = event.author
        val content = event.message.contentRaw

        if (event.isWebhookMessage || author.isBot || content.isBlank() || !content.startsWith(prefix)) {
            return
        }

        val args = content.substring(prefix.length)
            .trim()
            .split("\\s+".toRegex())
            .toMutableList()

        // Interesting case
        if (args.isEmpty()) {
            return
        }

        val invoke = args.removeAt(0).toLowerCase()
        var command: Command? = null

        if (commands.containsKey(invoke)) {
            command = commands[invoke]
        } else if (aliases.containsKey(invoke)) {
            command = commands[aliases[invoke]]
        }

        if (command == null) {
            return
        }

        val commandName = command.name

        // Run the commands asynchronously so they don't block the event thread
        GlobalScope.launch {
            logger.info("Running command $commandName in ${event.guild} with $args")

            try {
                command.run(args, event)
            } catch (e: Throwable) {
                event.channel.sendMessage("${author.asMention}, there was an error trying to execute that command!")
                    .queue()
                logger.error("Command $commandName failed in ${event.guild} with $args", e)
            }
        }
    }

    // No need to worry about indexing with null, it just returns null
    fun getCommand(commandName: String) = commands[commandName] ?: commands[aliases[commandName]]
}
