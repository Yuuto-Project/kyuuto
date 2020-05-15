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

import com.jagrosh.jdautilities.commons.utils.FinderUtil
import io.github.yuutoproject.yuutobot.commands.base.AbstractCommand
import io.github.yuutoproject.yuutobot.commands.base.CommandCategory
import io.github.yuutoproject.yuutobot.objects.ShipMessage
import net.dv8tion.jda.api.entities.Member
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent
import net.dv8tion.jda.api.utils.data.DataArray

class Ship : AbstractCommand(
    "ship",
    CommandCategory.FUN,
    "Yuuto mastered the art of shipping users and can now calculate if you and your crush will work out",
    "<user1> <user2>"
) {
    // ship message list or map with scores?
    private val shipMessages: Map<Int, String>
    private val riggedUsers = mapOf<Long, Long>()

    init {
        // use data class?
        ShipMessage(0, "")

        val messagesMap = hashMapOf<Int, String>()
        val json = DataArray.fromJson(this.javaClass.getResourceAsStream("/ship_messages.json"))

        for (index in 0 until json.length()) {
            val data = json.getObject(index)

            messagesMap[data.getInt("max_score")] = data.getString("message")
        }

        // turn the map into a read only map
        shipMessages = messagesMap.toMap()
    }

    override fun run(args: MutableList<String>, event: GuildMessageReceivedEvent) {
        // Get members from JDA_utils search
        // find a place to store the rigged ships that is not in the jar itself
        // TODO: (important) change all messages to CB related messages
        // ...
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
        return "TODO: REPLACE SHIP MESSAGES WITH CB RELATED ONES";
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
}
