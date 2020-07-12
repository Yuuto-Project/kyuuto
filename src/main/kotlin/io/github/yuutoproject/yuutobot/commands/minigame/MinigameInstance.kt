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
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent
import net.dv8tion.jda.api.events.message.guild.react.GuildMessageReactionAddEvent
import net.dv8tion.jda.api.events.message.guild.react.GuildMessageReactionRemoveEvent
import java.awt.Color

class MinigameInstance(
    private val questions: MutableList<Question>,
    // The ID of the channel the game is in
    // Effectively the ID of the game instance itself
    val id: Long,
    // This is necessary for when the game is finished and we want to remove the object
    private val manager: Minigame,
    private val maxRounds: Int
) {
    // Is the game in progress or not?
    var begun = false

    // String ID of user and their scores
    var players = mutableMapOf<Long, Int>()
    var timer = System.currentTimeMillis()

    private var rounds = 1

    private lateinit var startingMessageID: String

    private lateinit var currentQuestion: Question
    private lateinit var currentAnswers: MutableList<String>

    fun start(event: GuildMessageReceivedEvent) {
        event.channel.sendMessage("Starting a game with $maxRounds rounds...").queue()

        val embed = EmbedBuilder()
            .setColor(Color.BLUE)
            .setTitle("Minigame Starting!")
            .setDescription(
                "React below to join the game! \n" +
                    "This game may contain spoilers or NSFW themes.\n" +
                    "Please run `minigame skip` in order to skip a question."
            )
        val startingMessage = event.channel.sendMessage(embed.build()).complete()
        startingMessageID = startingMessage.id

        startingMessage.addReaction("U+1F1F4").complete()

        for (countdown in 10 downTo 0 step 2) {
            embed.setDescription(
                "React below to join the game!\n" +
                    "This game may contain spoilers or NSFW themes.\n" +
                    "Please run `minigame skip` in order to skip a question.\n" +
                    "Current players: ${getPlayers()}\n" +
                    "$countdown seconds left!"
            )
            startingMessage.editMessage(embed.build()).complete()

            Thread.sleep(2000)
        }

        if (players.isEmpty()) {
            embed.setTitle("Minigame cancelled!").setDescription("Nobody joined...")
            startingMessage.editMessage(embed.build()).complete()
            manager.unregister(this)
            return
        }

        embed.setTitle("Minigame started!").setDescription("Game has begun!")
        startingMessage.editMessage(embed.build()).complete()

        begun = true
        progress(event)
    }

    fun progress(event: GuildMessageReceivedEvent) {
        if (rounds > maxRounds) {
            endGame(event)
            return
        }

        // Unfortunately, no removeOrNull, so we have to use a try/catch
        try {
            currentQuestion = questions.removeAt(0)
        } catch (e: IndexOutOfBoundsException) {
            endGame(event)
            return
        }

        currentAnswers = currentQuestion.answers.map { it.toLowerCase() }
            .toMutableList()

        if (currentQuestion.type == "FILL") {
            event.channel.sendMessage(currentQuestion.question).queue()
        } else if (currentQuestion.type == "MULTIPLE") {
            val questionString = "${currentQuestion.question}\n"

            val answerString = (currentQuestion.wrong + currentQuestion.answers).shuffled()
                .mapIndexed { i, answer ->
                    if (currentAnswers.contains(answer.toLowerCase())) {
                        currentAnswers.add((i + 1).toString())
                    }

                    "${i + 1}) $answer"
                }.joinToString("\n")

            event.channel.sendMessage(questionString + answerString).queue()
        }
    }

    private fun endGame(event: GuildMessageReceivedEvent) {
        // Sort by descending value and then map each value to a line in the scoreboard, then join it
        val scoreboard = players.entries.sortedByDescending { it.value }.mapIndexed { i, entry ->
            "${i + 1}) <@${entry.key}> with ${entry.value} points"
        }.joinToString("\n")

        val embed = EmbedBuilder()
            .setColor(Color.BLUE)
            .setTitle("Minigame ended!")
            .setDescription("Total points:\n$scoreboard")
        event.channel.sendMessage(embed.build()).queue()

        manager.unregister(this)
    }

    fun answerReceived(event: GuildMessageReceivedEvent) {
        // A new guess is made, so we reset the stale-game timer
        timer = System.currentTimeMillis()

        if (currentAnswers.contains(event.message.contentStripped.toLowerCase())) {
            players[event.author.idLong] = players[event.author.idLong]!! + 1
            event.channel.sendMessage("${event.author.name} got the point!").queue()
            rounds += 1
            progress(event)
        }
    }

    fun reactionRecv(event: GuildMessageReactionAddEvent) {
        if (
            event.user.isBot ||
            players.contains(event.user.idLong) ||
            begun ||
            event.messageId != startingMessageID
        ) return

        players[event.user.idLong] = 0
    }

    fun reactionRetr(event: GuildMessageReactionRemoveEvent) {
        if (
            event.messageId == startingMessageID &&
            !begun
        ) {
            players.remove(event.user!!.idLong)
        }
    }

    private fun getPlayers() = if (players.isNotEmpty()) {
        players.keys.joinToString(", ") { "<@$id>" }
    } else {
        "none"
    }
}
