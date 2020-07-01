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

package io.github.yuutoproject.yuutobot.commands.minigame

import io.github.yuutoproject.yuutobot.commands.Minigame
import io.github.yuutoproject.yuutobot.objects.Question
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.entities.User
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent
import net.dv8tion.jda.api.events.message.guild.react.GuildMessageReactionAddEvent
import net.dv8tion.jda.api.events.message.guild.react.GuildMessageReactionRemoveEvent
import java.awt.Color

enum class State {
    STARTING,
    IN_PROGRESS
}

class MinigameInstance(
    private val questions: MutableList<Question>,
    val channelID: String,
    private val minigameManager: Minigame
) {
    private var state = State.STARTING
    private var players = mutableMapOf<User, Int>()
    private var countdown = 0
    private var rounds = 0
    private var timer = System.currentTimeMillis()

    private lateinit var startingMessageID: String

    private lateinit var currentQuestion: Question
    private lateinit var answers: MutableList<String>

    fun run(args: MutableList<String>, event: GuildMessageReceivedEvent) {
        if (
            state == State.IN_PROGRESS &&
            args.size > 0 &&
            args[0] == "skip" &&
            players.contains(event.author)
        ) {
            event.channel.sendMessage("Skipping the question...").queue()
            progress(event)
            return
        }

        if (state == State.IN_PROGRESS) {
            if (System.currentTimeMillis() - timer > 3000) {
                event.channel.sendMessage("Cancelling stale game...").queue()
            } else {
                event.channel.sendMessage("A game is already running!")
                return
            }
        }

        state = State.STARTING
        players = mutableMapOf()
        countdown = 0
        rounds = 0
        timer = System.currentTimeMillis()

        // Starting

        val embed = EmbedBuilder()
            .setColor(Color.BLUE)
            .setTitle("Minigame Starting!")
            .setDescription(
                "React below to join the game! \n" +
                    "This game may contain spoilers or NSFW themes.\nPlease run `minigame skip` in order to skip a question."
            )
        val startingMessage = event.channel.sendMessage(embed.build()).complete()
        startingMessageID = startingMessage.id

        startingMessage.addReaction("U+1F1F4").complete()

        for (countdown in 10 downTo 0 step 2) {
            embed.setDescription(
                "React below to join the game! \nThis game may contain spoilers or NSFW themes.\nPlease run `minigame skip` in order to skip a question.\nCurrent players: ${getPlayers()}\n $countdown seconds left!"
            )
            startingMessage.editMessage(embed.build()).complete()

            Thread.sleep(2000)
        }

        if (players.isEmpty()) {
            embed.setTitle("Minigame cancelled!").setDescription("Nobody joined...")
            startingMessage.editMessage(embed.build()).complete()
            minigameManager.unregister(event.jda, this)
            return
        }

        embed.setTitle("Minigame started!").setDescription("Game has begun!")
        startingMessage.editMessage(embed.build()).complete()

        state = State.IN_PROGRESS
        progress(event)
    }

    private fun progress(event: GuildMessageReceivedEvent) {
        if (rounds > 1) {
            endGame(event)
            return
        }

        try {
            currentQuestion = questions.removeAt(0)
        } catch (e: IndexOutOfBoundsException) {
            endGame(event)
            return
        }

        answers = currentQuestion.answers.map { it.toLowerCase() }
            .toMutableList()

        if (currentQuestion.type == "FILL") {
            event.channel.sendMessage(currentQuestion.question).queue()
        } else if (currentQuestion.type == "MULTIPLE") {
            val questionString = "${currentQuestion.question}\n"

            val answerString = (currentQuestion.wrong + currentQuestion.answers).shuffled()
                .mapIndexed { i, answer ->
                    if (answers.contains(answer.toLowerCase())) {
                        answers.add((i + 1).toString())
                    }

                    "${i + 1}) $answer"
                }.joinToString("\n")

            event.channel.sendMessage(questionString + answerString).queue()
        }
    }

    private fun endGame(event: GuildMessageReceivedEvent) {
        val embed = EmbedBuilder()
            .setColor(Color.BLUE)
            .setTitle("Minigame ended!")
            .setDescription("Total points:\n${getScoreboard()}")
        event.channel.sendMessage(embed.build()).queue()
        minigameManager.unregister(event.jda, this)
    }

    fun messageRecv(event: GuildMessageReceivedEvent) {
        if (
            state != State.IN_PROGRESS ||
            !players.contains(event.author)
        ) return

        timer = System.currentTimeMillis()

        if (answers.contains(event.message.contentStripped.toLowerCase())) {
            players[event.author] = players[event.author]!! + 1
            event.channel.sendMessage("${event.author.name} got the point!").queue()
            rounds += 1
            progress(event)
        }
    }

    fun reactionRecv(event: GuildMessageReactionAddEvent) {
        if (event.user.isBot ||
            players.contains(event.user) ||
            state != State.STARTING ||
            event.messageId != startingMessageID
        ) return

        players[event.user] = 0
    }

    fun reactionRetr(event: GuildMessageReactionRemoveEvent) {
        if (state == State.STARTING) players.remove(event.user)
    }

    private fun getScoreboard(): String {
        val sortedPlayers = players.entries.sortedByDescending { it.value }

        return sortedPlayers.mapIndexed { i, entry ->
            "${i + 1}) ${entry.key.asMention} with ${entry.value} points"
        }.joinToString("\n")
    }

    private fun getPlayers() = if (players.isNotEmpty()) {
        players.keys.joinToString(", ") { it.asMention }
    } else {
        "none"
    }
}
