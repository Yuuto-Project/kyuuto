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
import io.github.yuutoproject.yuutobot.utils.EMOJI_REGEX
import io.github.yuutoproject.yuutobot.utils.EMOTE_MENTIONS_REGEX
import io.github.yuutoproject.yuutobot.utils.NONASCII_REGEX
import io.github.yuutoproject.yuutobot.utils.httpClient
import java.io.IOException
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent
import net.dv8tion.jda.api.utils.data.DataObject
import net.dv8tion.jda.internal.utils.IOUtil
import okhttp3.*
import okhttp3.Callback as OkHttp3Callback
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import org.slf4j.LoggerFactory

class Dialog : AbstractCommand("dialog", CommandCategory.INFO, "does something", "[bg] <char> <text>") {
    private val logger = LoggerFactory.getLogger(this.javaClass)

    private val backgrounds = listOf(
        "bath",
        "beach",
        "cabin",
        "camp",
        "cave",
        "forest",
        "messhall"
    )

    private val characters = listOf(
        "aiden",
        "avan",
        "chiaki",
        "connor",
        "eduard",
        "felix",
        "goro",
        "hiro",
        "hunter",
        "jirou",
        "keitaro",
        "kieran",
        "knox",
        "lee",
        "naoto",
        "natsumi",
        "seto",
        "taiga",
        "yoichi",
        "yoshi",
        "yuri",
        "yuuto"
    )

    private val backgroundsString = "`${backgrounds.joinToString("`, `")}`"
    private val charactersString = "`${characters.joinToString("`, `")}`"

    private val json = "application/json; charset=utf-8".toMediaType()

    // Using ExperimentalStdLib for .removeFirst() in Mutable Lists
    @ExperimentalStdlibApi
    override fun run(args: MutableList<String>, event: GuildMessageReceivedEvent) {
        val now: Long = System.currentTimeMillis()

        if (args.count() < 2) {
            event.channel.sendMessage("This command requires two arguments : `dialog [background] <character> <text>` ([] is optional)").queue()
            return
        }

        var character = args.removeFirst().toLowerCase()
        val background: String

        if (characters.contains(character)) {
            background = "camp"
        } else {
            background = character
            character = args.removeFirst().toLowerCase()
        }

        if (!backgrounds.contains(background)) {
            event.channel.sendMessage("Sorry but I couldn't find `$background` as a location\nAvailable backgrounds are: $backgroundsString").queue()
            return
        }

        if (!characters.contains(character)) {
            event.channel.sendMessage("Sorry, but I don't think that `$character` is a character in Camp Buddy\nAvailable characters are: $charactersString").queue()
            return
        }

        if (args.count() < 1) {
            event.channel.sendMessage("Please provide a message to be written~!").queue()
            return
        }

        val text: String = args.joinToString(" ").replace("/[‘’]/g".toRegex(), "'")

        if (text.length > 120) {
            event.channel.sendMessage("Sorry, the message limit is 120 characters <:hiroJey:692008426842226708>").queue()
            return
        }

        if (
            EMOTE_MENTIONS_REGEX.containsMatchIn(text) ||
            EMOJI_REGEX.containsMatchIn(text) ||
            NONASCII_REGEX.containsMatchIn(text)
        ) {
            event.channel.sendMessage("Sorry, I can't display emotes, mentions, or non-latin characters").queue()
            return
        }

        event.channel.sendTyping().queue()

        val body = DataObject.empty()
            .put("background", background)
            .put("character", character)
            .put("text", text)
            .toString().toRequestBody(json)

        val request = Request.Builder()
            .url("https://kyuu.to/dialog")
            .post(body)
            .build()

        httpClient.newCall(request).enqueue(object : OkHttp3Callback {
            override fun onFailure(call: Call, e: IOException) {
                event.channel.sendMessage("An error just happened in me, blame the devs <:YoichiLol:701312070880329800>").queue()
            }

            override fun onResponse(call: Call, response: Response) {
                if (response.code == 429) {
                    event.channel.sendMessage("I can't handle this much load at the moment, maybe try again later? <:YoichiPlease:692008252690530334>")
                    return
                } else if (response.code != 200) {
                    event.channel.sendMessage("An error just happened in me, blame the devs <:YoichiLol:701312070880329800>").queue()
                    return
                }

                val stream = IOUtil.readFully(IOUtil.getBody(response))

                event.channel.sendFile(stream, "file.png")
                    .append(event.author.asMention)
                    .append(", Here ya go!")
                    .queue()

                logger.info("Generated image for $character at $background, took ${System.currentTimeMillis() - now}ms")
            }
        })
    }
}
