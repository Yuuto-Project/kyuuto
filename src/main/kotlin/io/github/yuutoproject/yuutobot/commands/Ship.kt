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

import com.fasterxml.jackson.core.json.JsonReadFeature
import com.fasterxml.jackson.databind.json.JsonMapper
import com.jagrosh.jdautilities.commons.utils.FinderUtil
import io.github.yuutoproject.yuutobot.commands.base.AbstractCommand
import io.github.yuutoproject.yuutobot.commands.base.CommandCategory
import net.dv8tion.jda.api.entities.Member
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent

class Ship : AbstractCommand(
    "ship",
    CommandCategory.FUN,
    "Yuuto mastered the art of shipping users and can now calculate if you and your crush will work out",
    "<user1> <user2>"
) {
    // ship message list or map with scores?
    private val shipMessages: Map<Int, String>
    private val riggedUsers: Map<Long, Long>

    init {
        // We're using jackson here so we can allow for comments in our json
        val mapper = JsonMapper.builder()
            .enable(
                JsonReadFeature.ALLOW_TRAILING_COMMA,
                JsonReadFeature.ALLOW_JAVA_COMMENTS,
                JsonReadFeature.ALLOW_YAML_COMMENTS,
                JsonReadFeature.ALLOW_UNQUOTED_FIELD_NAMES
            )
            .build()

        // mutable map keeps the order, a hashmap does not
        val messagesMap = mutableMapOf<Int, String>()
        val json = mapper.readTree(this.javaClass.getResource("/ship_messages.json5"))

        json.forEach {
            messagesMap[it.get("max_score").asInt()] = it.get("message").asText()
        }

        // turn the map into a read only map
        this.shipMessages = messagesMap.toMap()

        val riggedMap = hashMapOf<Long, Long>()
        // TODO: Relocate this
        val riggedJson = mapper.readTree(this.javaClass.getResource("/rigged_ships.json5"))

        riggedJson.fieldNames().forEach {
            riggedMap[it.toLong()] = riggedJson.get(it).asLong()
        }

        this.riggedUsers = riggedMap.toMap()
    }

    override fun run(args: MutableList<String>, event: GuildMessageReceivedEvent) {
        // Get members from JDA_utils search
        // find a place to store the rigged ships that is not in the jar itself
        // TODO: (important) change all messages to CB related messages
        // ...

        val channel = event.channel

        if (args.size < 2) {
            channel.sendMessage("Missing args").queue()
            return
        }

        val name1 = args[0]
        val member1 = this.findMember(name1, event)

        if (member1 == null) {
            channel.sendMessage("Missing member 1").queue()
            return
        }

        val name2 = args[1]
        val member2 = this.findMember(name2, event)

        if (member2 == null) {
            channel.sendMessage("Missing member 2").queue()
            return
        }

        val (score, message) = this.getScoreAndMessage(member1, member2)


        channel.sendMessage("${member1.user.asTag}x${member2.user.asTag} - $score - $message").queue()
    }

    private fun findMember(input: String, event: GuildMessageReceivedEvent): Member? {
        val foundMembers = FinderUtil.findMembers(input, event.guild)

        if (foundMembers.isEmpty()) {
            return null
        }

        // why not "?: null"
        // Well java is fun and will throw an index out of bounds exception if there is no first element
        return foundMembers[0]
    }

    private fun shouldBeRigged(member1: Member, member2: Member): Boolean {
        val id1 = member1.idLong
        val id2 = member2.idLong

        // May crash due to the map returning null if it doesn't have the key
        return this.riggedUsers[id1] == id2 || this.riggedUsers[id2] == id1
    }

    private fun calculateScore(member1: Member, member2: Member): Int {
        val score = (member1.idLong + member2.idLong) / 7

        // convert the long to an int
        return (score % 100).toInt()
    }

    private fun getMessageFromScore(score: Int): String {
        val scoreKey = this.shipMessages.keys.first { score <= it }

        return this.shipMessages[scoreKey] ?: error("Excuse me but how is this even possible?")
    }

    private fun getScoreAndMessage(member1: Member, member2: Member): Pair<Int, String> {
        if (this.shouldBeRigged(member1, member2)) {
            // We're using the getMessageFromScore method here so it uses the message from the json
            // this way we only have to change the message in one place when we update it
            return 100 to this.getMessageFromScore(100)
        }

        val score = this.calculateScore(member1, member2)
        val message = this.getMessageFromScore(score)

        return score to message
    }

    companion object {
        // main method for good old java in the good old java way
        @JvmStatic
        fun main(args: Array<String>) {
            val ship = Ship()

            for (i in 0..100) {
                println("$i - ${ship.getMessageFromScore(i)}")
            }
        }
    }
}
