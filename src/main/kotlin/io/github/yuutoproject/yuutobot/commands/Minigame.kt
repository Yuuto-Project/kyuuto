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

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.core.type.TypeReference
import io.github.yuutoproject.yuutobot.commands.base.AbstractCommand
import io.github.yuutoproject.yuutobot.commands.base.CommandCategory
import io.github.yuutoproject.yuutobot.utils.jackson
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.entities.Message
import net.dv8tion.jda.api.entities.User
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent
import net.dv8tion.jda.api.events.message.guild.react.GuildMessageReactionAddEvent
import net.dv8tion.jda.api.events.message.guild.react.GuildMessageReactionRemoveEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter
import java.awt.Color

enum class State {
    OFF,
    STARTING,
    IN_PROGRESS
}

class MinigameListener(val minigame: Minigame) : ListenerAdapter() {
    override fun onGuildMessageReceived(event: GuildMessageReceivedEvent) {
        minigame.messageRecv(event)
    }

    override fun onGuildMessageReactionAdd(event: GuildMessageReactionAddEvent) {
        minigame.reactionRecv(event)
    }

    override fun onGuildMessageReactionRemove(event: GuildMessageReactionRemoveEvent) {
        minigame.reactionRetr(event)
    }
}

class Question @JsonCreator constructor(
    @JsonProperty("type") val type: String,
    @JsonProperty("question") val question: String,
    @JsonProperty("answers") val answers: MutableList<String>,
    @JsonProperty("wrong") val wrong: List<String>
)

class Minigame : AbstractCommand(
    "minigame",
    CommandCategory.GAME,
    "Play a fun quiz with your friends!",
    "Run `minigame` to begin a new game, and react within the countdown to join.\nRun `minigame skip` to skip a question you do not wish to answer."
) {
    var state = State.OFF
    var players = mutableMapOf<User, Int>()
    var countdown = 0
    var rounds = 0
    var timer = System.currentTimeMillis()

    lateinit var startingMessage: Message
    lateinit var questions: MutableList<Question>

    lateinit var currentQuestion: Question
    lateinit var answers: MutableList<String>

    val messageListener = MinigameListener(this)

    fun getShuffledQuestions(): MutableList<Question> {
        val json = jackson.readTree(this.javaClass.getResource("/minigame.json"))
        val questions = jackson.readValue(json.traverse(), object : TypeReference<MutableList<Question>>() {})
        questions.shuffle()
        return questions
    }

    fun initialize() {
        state = State.OFF
        players = mutableMapOf()
        countdown = 0
        questions = getShuffledQuestions()
        rounds = 0
        timer = System.currentTimeMillis()
    }

    override fun run(args: MutableList<String>, event: GuildMessageReceivedEvent) {
        if (
            state == State.IN_PROGRESS &&
            args.size > 0 &&
            args[0] == "skip" &&
            players.contains(event.author)
        ) {
            event.channel.sendMessage("Skipping the question...").queue()
            progress(event.jda)
            return
        }

        if (
            state == State.IN_PROGRESS &&
            System.currentTimeMillis() - timer > 3000
        ) {
            event.channel.sendMessage("Cancelling stale game...").queue()
            clean(event.jda)
            return
        }

        if (state != State.OFF) {
            event.channel.sendMessage("A game is already running!").queue()
            return
        }

        this.initialize()

        event.jda.addEventListener(messageListener)

        // Starting

        state = State.STARTING

        val embed = EmbedBuilder()
            .setColor(Color.BLUE)
            .setTitle("Minigame Starting!")
            .setDescription(
                "React below to join the game! \n" +
                    "This game may contain spoilers or NSFW themes.\nPlease run `minigame skip` in order to skip a question."
            )
        startingMessage = event.channel.sendMessage(embed.build()).complete()

        startingMessage.addReaction("U+1F1F4").complete()

        for (countdown in 10 downTo 0 step 2) {
            embed.setDescription(
                "React below to join the game! \nThis game may contain spoilers or NSFW themes.\nPlease run `minigame skip` in order to skip a question.\nCurrent players: ${players.keys}\n $countdown seconds left!"
            )
            startingMessage.editMessage(embed.build()).complete()

            Thread.sleep(2000)
        }

        if (players.isEmpty()) {
            embed.setTitle("Minigame cancelled!").setDescription("Nobody joined...")
            startingMessage.editMessage(embed.build()).complete()
            clean(event.jda)
            return
        }

        embed.setTitle("Minigame started!").setDescription("Game has begun!")
        startingMessage.editMessage(embed.build()).complete()

        state = State.IN_PROGRESS
        progress(event.jda)
    }

    fun progress(client: JDA) {
        if (rounds > 1) {
            endGame(client)
            return
        }

        try {
            currentQuestion = questions.removeAt(0)
        } catch (e: IndexOutOfBoundsException) {
            endGame(client)
            return
        }

        answers = currentQuestion.answers.map { it.toLowerCase() }
            .toMutableList()

        if (currentQuestion.type == "FILL") {
            startingMessage.channel.sendMessage(currentQuestion.question).queue()
        } else if (currentQuestion.type == "MULTIPLE") {
            val questionString = "${currentQuestion.question}\n"

            val answerString = (currentQuestion.wrong + currentQuestion.answers).shuffled()
                .mapIndexed { i, answer ->
                    if (answers.contains(answer.toLowerCase())) {
                        answers.add((i + 1).toString())
                    }

                    "${i + 1}) $answer"
                }.joinToString("\n")

            startingMessage.channel.sendMessage(questionString + answerString).queue()
        }
    }

    fun endGame(client: JDA) {
        val embed = EmbedBuilder()
            .setColor(Color.BLUE)
            .setTitle("Minigame ended!")
            .setDescription("Total points:\n${getScoreboard()}")
        startingMessage.channel.sendMessage(embed.build()).queue()

        clean(client)
    }

    fun clean(client: JDA) {
        client.removeEventListener(messageListener)
        state = State.OFF
    }

    fun messageRecv(event: GuildMessageReceivedEvent) {
        if (
            state != State.IN_PROGRESS ||
            event.channel != startingMessage.channel ||
            !players.contains(event.author)
        ) return

        timer = System.currentTimeMillis()

        println(answers)
        println(event.message.contentStripped.toLowerCase())

        if (answers.contains(event.message.contentStripped.toLowerCase())) {
            players[event.author] = players[event.author]!! + 1
            event.channel.sendMessage("${event.author.name} got the point!").queue()
            rounds += 1
            progress(event.jda)
        }
    }

    fun reactionRecv(event: GuildMessageReactionAddEvent) {
        if (event.user.isBot ||
            players.contains(event.user) ||
            state != State.STARTING ||
            event.messageId != startingMessage.id
        ) return

        players[event.user] = 0
    }

    fun reactionRetr(event: GuildMessageReactionRemoveEvent) {
        if (state == State.STARTING) players.remove(event.user)
    }

    fun getScoreboard(): String {
        val sortedPlayers = players.entries.sortedByDescending { it.value }

        return sortedPlayers.mapIndexed { i, entry ->
            "${i + 1}) ${entry.key.name} with ${entry.value} points"
        }.joinToString("\n")
    }
}
