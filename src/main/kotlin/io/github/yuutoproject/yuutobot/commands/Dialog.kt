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
import java.io.IOException
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent
import net.dv8tion.jda.internal.utils.IOUtil
import okhttp3.*
import okhttp3.Callback as OkHttp3Callback
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody

val backgrounds : List<String> = listOf(
    "bath",
    "beach",
    "cabin",
    "camp",
    "cave",
    "forest",
    "messhall"
)

val characters : List<String> = listOf(
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

val backgroundsString : String = backgrounds.joinToString("`, `")
val charactersString : String = characters  .joinToString("`, `")

class Dialog : AbstractCommand("dialog", CommandCategory.INFO, "does something", "[bg] <char> <text>") {
    //Using ExperimentalStdLib for .removeFirst() in Mutable Lists
    @ExperimentalStdlibApi
    override fun run(args: MutableList<String>, event: GuildMessageReceivedEvent) {
        val moveThisToAStorageLocation = OkHttpClient()

        if(args.count() < 2){
            event.channel.sendMessage("This command requires two arguments : `dialog [background] <character> <text>` ([] is optional)").queue()
            return
        }

        var character : String = args.removeFirst().toLowerCase()
        val background : String

        if(characters.contains(character)){
            background = "camp"
        } else {
            background = character
            character = args.removeFirst().toLowerCase()
        }

        if(!backgrounds.contains(background)){
            event.channel.sendMessage("Sorry but I couldn't find $background as a location\n available backgrounds are : `$backgroundsString`").queue()
            return
        }

        if(!characters.contains(character)){
            event.channel.sendMessage("Sorry, but I don't think that $character is a character in Camp Buddy\nAvailable characters are : `$charactersString`").queue()
            return
        }

        if(args.count() < 1){
            event.channel.sendMessage("Please provide a message to be written~!").queue()
            return
        }

        val text : String = args.joinToString(" ").replace("/[‘’]/g".toRegex(), "'")

        if(
            EMOTE_MENTIONS_REGEX.containsMatchIn(text) ||
            EMOJI_REGEX.containsMatchIn(text) ||
            NONASCII_REGEX.containsMatchIn(text)
        ) {
            event.channel.sendMessage("Sorry, I can't display emotes, mentions, or non-latin characters").queue()
            return
        }

        event.channel.sendTyping().queue()

        val json = "application/json; charset=utf-8".toMediaType()
        val body = """{
                "background": "$background",
                "character": "$character",
                "text": "$text"
            }""".trimIndent().toRequestBody(json)

        val request = Request.Builder()
            .url("https://yuuto.dunctebot.com/dialog")
            .post(body)
            .build()


        moveThisToAStorageLocation.newCall(request).enqueue(object : OkHttp3Callback {
            override fun onFailure(call: Call, e: IOException) {
                event.channel.sendMessage("Uh oh, an error just occurred. im sorry").queue()
            }

            override fun onResponse(call: Call, response: Response) {
                if(response.code != 200){
                    event.channel.sendMessage("Uh oh, an error just occurred. im sorry").queue()
                    return
                }

                val stream = IOUtil.readFully(IOUtil.getBody(response))

                event.channel.sendFile(stream, "file.png")
                    .append(event.author.asMention)
                    .append(", Here ya go!")
                    .queue()
            }
        })
    }
}
