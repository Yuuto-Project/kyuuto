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
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent
import net.dv8tion.jda.api.events.message.guild.react.GuildMessageReactionAddEvent
import net.dv8tion.jda.api.events.message.guild.react.GuildMessageReactionRemoveEvent

class Minigame : AbstractCommand(
    "minigame",
    CommandCategory.FUN,
    "Play a fun quiz with your friends!",
    "Run `minigame` to begin a new game, and react within the countdown to join.\nRun `minigame skip` to skip a question you do not wish to answer."
) {
    // String is the channel ID
    private var minigames = mutableMapOf<Long, MinigameInstance>()
    private val listener = MinigameListener(this)
    private val questions: List<Question>

    init {
        val json = jackson.readTree(this.javaClass.getResource("/minigame.json"))
        questions = jackson.readValue(json.traverse(), object : TypeReference<List<Question>>() {})
    }

    override fun run(args: MutableList<String>, event: GuildMessageReceivedEvent) {
        val id = event.channel.idLong

        // Handling for when a game is already in progress
        val minigame = minigames[id]

        if (minigame != null) {
            // If a user attempts to skip a question
            if (args.getOrNull(0) == "skip") {
                if (!minigame.players.contains(event.author.idLong)) {
                    event.channel.sendMessage("You can't skip a question if you aren't in the game!").queue()
                    return
                }

                if (!minigame.begun) {
                    event.channel.sendMessage("The game has not started yet!").queue()
                    return
                }

                event.channel.sendMessage("Skipping question...").queue()
                minigame.progress(event)
                return
            }

            // If the user attempts to start a new game while a game is already in progress,
            // Either cancel it if it's stale or indicate that a game is already in progress
            if (System.currentTimeMillis() - minigame.timer > 30_000) {
                event.channel.sendMessage("Cancelling stale game...").queue()
                unregister(minigame)
                // Continue outside of the if block and create a new game instance...
            } else {
                event.channel.sendMessage("A game is already running!").queue()
                return
            }
        }

        // Register our listener if it doesn't already exist
        if (!event.jda.registeredListeners.contains(listener)) {
            event.jda.addEventListener(listener)
        }

        val maxRounds = args.getOrNull(0)?.toIntOrNull() ?: 7
        if (maxRounds < 2 || maxRounds > 10) {
            event.channel.sendMessage("The number of rounds has to be greater than 1 and less than 11.").queue()
            return
        }

        minigames[id] = MinigameInstance(
            questions.shuffled().toMutableList(),
            event.channel.idLong,
            this,
            maxRounds
        )

        // Launch the game instance.
        GlobalScope.launch {
            minigames[id]!!.start(event)
        }
    }

    // Used by MinigameInstances to indicate that they're done
    fun unregister(minigame: MinigameInstance) {
        minigames.remove(minigame.id)
    }

    fun messageRecv(event: GuildMessageReceivedEvent) {
        val minigame = minigames[event.channel.idLong] ?: return

        if (!minigame.begun || !minigame.players.contains(event.author.idLong)) return

        minigame.answerReceived(event)
    }

    fun reactionRecv(event: GuildMessageReactionAddEvent) {
        minigames[event.channel.idLong]?.reactionRecv(event)
    }

    fun reactionRetr(event: GuildMessageReactionRemoveEvent) {
        minigames[event.channel.idLong]?.reactionRetr(event)
    }
}
