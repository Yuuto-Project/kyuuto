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
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent
import net.dv8tion.jda.api.events.message.guild.react.GuildMessageReactionAddEvent
import net.dv8tion.jda.api.events.message.guild.react.GuildMessageReactionRemoveEvent

class Minigame : AbstractCommand(
    "minigame",
    CommandCategory.GAME,
    "Play a fun quiz with your friends!",
    "Run `minigame` to begin a new game, and react within the countdown to join.\nRun `minigame skip` to skip a question you do not wish to answer."
) {
    // String is the channel ID
    private var minigameInstances = mutableMapOf<String, MinigameInstance>()
    private val messageListener = MinigameListener(this)
    private val questions: List<Question>

    init {
        val json = jackson.readTree(this.javaClass.getResource("/minigame.json"))
        questions = jackson.readValue(json.traverse(), object : TypeReference<List<Question>>() {})
    }

    override fun run(args: MutableList<String>, event: GuildMessageReceivedEvent) {
        // Register our listener if it doesn't already exist
        if (!event.jda.registeredListeners.contains(messageListener)) {
            event.jda.addEventListener(messageListener)
        }

        // If a game does not exist in that channel, create one
        if (!minigameInstances.contains(event.channel.id)) {
            minigameInstances[event.channel.id] =
                MinigameInstance(
                    questions.shuffled().toMutableList(),
                    event.channel.id,
                    this
                )
        }

        minigameInstances[event.channel.id]!!.run(args, event)
    }

    fun unregister(client: JDA, minigameInstance: MinigameInstance) {
        minigameInstances.remove(minigameInstance.channelID)

        // If no games are running, there's no point in listening for events
        if (minigameInstances.isEmpty()) {
            client.removeEventListener(messageListener)
        }
    }

    fun messageRecv(event: GuildMessageReceivedEvent) {
        minigameInstances[event.channel.id]?.messageRecv(event)
    }

    fun reactionRecv(event: GuildMessageReactionAddEvent) {
        minigameInstances[event.channel.id]?.reactionRecv(event)
    }

    fun reactionRetr(event: GuildMessageReactionRemoveEvent) {
        minigameInstances[event.channel.id]?.reactionRetr(event)
    }
}
