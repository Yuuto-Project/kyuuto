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

import com.fasterxml.jackson.core.type.TypeReference
import io.github.yuutoproject.yuutobot.commands.base.AbstractCommand
import io.github.yuutoproject.yuutobot.commands.base.CommandCategory
import io.github.yuutoproject.yuutobot.commands.minigame.MinigameInstance
import io.github.yuutoproject.yuutobot.commands.minigame.MinigameListener
import io.github.yuutoproject.yuutobot.objects.Question
import io.github.yuutoproject.yuutobot.utils.jackson
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.entities.TextChannel
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent
import net.dv8tion.jda.api.events.message.guild.react.GuildMessageReactionAddEvent
import net.dv8tion.jda.api.events.message.guild.react.GuildMessageReactionRemoveEvent

enum class State {
    OFF,
    STARTING,
    IN_PROGRESS
}

class Minigame : AbstractCommand(
    "minigame",
    CommandCategory.GAME,
    "Play a fun quiz with your friends!",
    "Run `minigame` to begin a new game, and react within the countdown to join.\nRun `minigame skip` to skip a question you do not wish to answer."
) {
    var minigameInstances = mutableMapOf<TextChannel, MinigameInstance>()
    val messageListener = MinigameListener(this)
    val questions: List<Question>

    lateinit var client: JDA

    init {
        val json = jackson.readTree(this.javaClass.getResource("/minigame.json"))
        questions = jackson.readValue(json.traverse(), object : TypeReference<List<Question>>() {})
    }

    override fun run(args: MutableList<String>, event: GuildMessageReceivedEvent) {
        if (minigameInstances.contains(event.channel) &&
            args.size > 0 &&
            args[0] != "skip"
        ) {
            event.channel.sendMessage("A game is already running!")
            return
        }

        if (!minigameInstances.contains(event.channel)) {
            minigameInstances[event.channel] =
                MinigameInstance(
                    questions.toMutableList(),
                    event.channel,
                    this
                )
        }

        // Register our listener if it doesn't already exist
        if (!event.jda.registeredListeners.contains(messageListener)) {
            client = event.jda
            event.jda.addEventListener(messageListener)
        }

        minigameInstances[event.channel]!!.run(args, event)
    }

    fun unregister(minigameInstance: MinigameInstance) {
        minigameInstances.remove(minigameInstance.channel)

        // If no games are running, there's no point in listening for events
        if (minigameInstances.isEmpty()) {
            client.removeEventListener(messageListener)
        }
    }

    fun messageRecv(event: GuildMessageReceivedEvent) {
        if (!minigameInstances.contains(event.channel)) return

        minigameInstances[event.channel]!!.messageRecv(event)
    }

    fun reactionRecv(event: GuildMessageReactionAddEvent) {
        if (!minigameInstances.contains(event.channel)) return

        minigameInstances[event.channel]!!.reactionRecv(event)
    }

    fun reactionRetr(event: GuildMessageReactionRemoveEvent) {
        if (!minigameInstances.contains(event.channel)) return

        minigameInstances[event.channel]!!.reactionRetr(event)
    }
}
