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
import io.github.yuutoproject.yuutobot.extensions.applyDefaults
import io.github.yuutoproject.yuutobot.extensions.getStaticAvatarUrl
import io.github.yuutoproject.yuutobot.utils.findMember
import io.github.yuutoproject.yuutobot.utils.httpClient
import io.github.yuutoproject.yuutobot.utils.jackson
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.entities.Member
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent
import net.dv8tion.jda.internal.utils.IOUtil
import okhttp3.Call
import okhttp3.Request
import okhttp3.Response
import java.io.File
import java.io.IOException
import okhttp3.Callback as OkHttp3Callback

class Ship : AbstractCommand(
    "ship",
    CommandCategory.FUN,
    "Yuuto mastered the art of shipping users and can now calculate if you and your crush will work out",
    "<user1> <user2>",
    ""
) {
    private val shipMessages: Map<Int, String>
    private val riggedUsers: Map<Long, Long>
    private val selfShipMessages = listOf(
        "Unsurprising, loving yourself is all you do around here.",
        "Who would've thought that you'd love yourself this much"
    )

    init {
        // mutable map keeps the order, a hashmap does not
        val messagesMap = mutableMapOf<Int, String>()
        val json = jackson.readTree(this.javaClass.getResource("/ship_messages.json5"))

        json.forEach {
            messagesMap[it.get("max_score").asInt()] = it.get("message").asText()
        }

        // turn the map into a read only map
        this.shipMessages = messagesMap.toMap()
        this.riggedUsers = this.loadRiggedShips()
    }

    override fun run(args: MutableList<String>, event: GuildMessageReceivedEvent) {
        val channel = event.channel

        if (args.size < 2) {
            channel.sendMessage("This command requires two arguments: `ship <user1> <user2>`.").queue()
            return
        }

        val name1 = args[0]
        val member1 = findMember(name1, event)

        if (member1 == null) {
            channel.sendMessage("No user found for input $name1").queue()
            return
        }

        val name2 = args[1]
        val member2 = findMember(name2, event)

        if (member2 == null) {
            channel.sendMessage("No user found for input $name2").queue()
            return
        }

        val (score, message) = this.getScoreAndMessage(member1, member2)
        val member1Avatar = member1.getStaticAvatarUrl()
        val member2Avatar = member2.getStaticAvatarUrl()
        val imageUrl = "https://api.alexflipnote.dev/ship?user=$member1Avatar&user2=$member2Avatar"

        val parsedMessage = if (message.contains("%s")) {
            message.format(member1.effectiveName, member2.effectiveName)
        } else {
            message
        }

        fetchUrlBytes(imageUrl) {
            val embed = EmbedBuilder()
                .setTitle("${member1.effectiveName} and ${member2.effectiveName}")
                .addField("Your love score is $score%", parsedMessage, false)
                .setImage("attachment://ship.png")
                .build()

            channel.sendFile(it, "ship.png")
                .embed(embed)
                .queue()
        }
    }

    private fun shouldBeRigged(member1: Member, member2: Member): Boolean {
        val id1 = member1.idLong
        val id2 = member2.idLong

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
        if (member1 == member2) {
            return 100 to selfShipMessages.random()
        }

        if (this.shouldBeRigged(member1, member2)) {
            // We're using the getMessageFromScore method here so it uses the message from the json
            // this way we only have to change the message in one place when we update it
            return 100 to this.getMessageFromScore(100)
        }

        val score = this.calculateScore(member1, member2)
        val message = this.getMessageFromScore(score)

        return score to message
    }

    private fun fetchUrlBytes(url: String, callback: (ByteArray) -> Unit) {
        val request = Request.Builder()
            .applyDefaults()
            .url(url)
            .build()

        httpClient.newCall(request).enqueue(object : OkHttp3Callback {
            override fun onFailure(call: Call, e: IOException) {
                e.printStackTrace() // the ugly way
            }

            override fun onResponse(call: Call, response: Response) {
                response.use {
                    val bytes = IOUtil.readFully(IOUtil.getBody(it))

                    callback(bytes)
                }
            }
        })
    }

    /**
     * Loads in all of the rigged ships from the json file stored IN THE ROOT OF THE CURRENT WORKING DIRECTORY
     * The format of the file is
     * { "user id": "user id" }
     * The order of the ids doesn't matter as the ap checks both ways
     */
    private fun loadRiggedShips(): Map<Long, Long> {
        val shipsFile = File("rigged_ships.json5")

        if (!shipsFile.exists()) {
            logger.warn("Skipping rigged ships as file does not exist")
            return mapOf()
        }

        val riggedMap = hashMapOf<Long, Long>()
        val riggedJson = jackson.readTree(shipsFile)

        riggedJson.fieldNames().forEach {
            riggedMap[it.toLong()] = riggedJson.get(it).asLong()
        }

        logger.info("Loaded rigged ships $riggedMap")

        return riggedMap.toMap()
    }
}
