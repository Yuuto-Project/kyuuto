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

import io.github.yuutoproject.yuutobot.commands.base.AbstractCommand
import io.github.yuutoproject.yuutobot.commands.base.CommandCategory
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.entities.Emote
import net.dv8tion.jda.api.entities.Message
import net.dv8tion.jda.api.entities.MessageEmbed
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

class Minigame : AbstractCommand(
    "minigame",
    CommandCategory.GAME,
    "Play a fun quiz with your friends!",
    "Run `minigame` to begin a new game, and react within the countdown to join.\nRun `minigame skip` to skip a question you do not wish to answer."
) {
    var state = State.OFF
    var players = mutableMapOf<User, Int>()
    var countdown = 0
    lateinit var startingMessage: Message
    var questions = null
    var currentQuestion = null
    var answers = null
    var rounds = 0
    var timer = System.currentTimeMillis()
    
    val messageListener = MinigameListener(this)
    
    fun initialize() {
        state = State.OFF
        players = mutableMapOf<User, Int>()
        countdown = 0
        questions = null
        currentQuestion = null
        answers = null
        rounds = 0
        timer = System.currentTimeMillis()
    }
    
    override fun run(args: MutableList<String>, event: GuildMessageReceivedEvent) {
        if (
            state == State.IN_PROGRESS &&
                args[0] == "skip" &&
                players.contains(event.author)
            ) {
            event.channel.sendMessage("Skipping the question...").queue()
//            progress()
            return
        }

        if (
            state == State.IN_PROGRESS &&
            System.currentTimeMillis() - timer > 3000
            ) {
            event.channel.sendMessage("Cancelling stale game...")
            clean(event.jda)
            return
        }

        if (state != State.OFF) {
            event.channel.sendMessage("A game is already running!")
            return
        }

        this.initialize()

        event.jda.addEventListener(messageListener)

        // Starting

        state = State.STARTING

        val embed = EmbedBuilder()
            .setColor(Color.BLUE)
            .setTitle("Minigame Starting!")
            .setDescription("React below to join the game! \n" +
                "\nThis game may contain spoilers or NSFW themes.\nPlease run `minigame skip` in order to skip a question.")
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
    }
    
    fun clean(client: JDA) {
        client.removeEventListener(messageListener)
        state = State.OFF
    }
    
    fun messageRecv(event: GuildMessageReceivedEvent) {
        print("Message received!")
    }
    
    fun reactionRecv(event: GuildMessageReactionAddEvent) {
        if (event.user.isBot ||
            players.contains(event.user) ||
            state != State.STARTING ||
            event.messageId != startingMessage.id) return

        players[event.user] = 0
    }
    
    fun reactionRetr(event: GuildMessageReactionRemoveEvent) {
        if (state == State.STARTING) players.remove(event.user)
    }
}
