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
import java.io.IOException
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent
import net.dv8tion.jda.internal.utils.IOUtil
import okhttp3.*
import okhttp3.Callback as OkHttp3Callback
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody

// TODO: get arch to implement the actual command
class Dialog : AbstractCommand("dialog", CommandCategory.INFO, "does something", "[bg] <char> <text>") {
    override fun run(args: MutableList<String>, event: GuildMessageReceivedEvent) {
        val moveThisToAStorageLocation = OkHttpClient()

        event.channel.sendTyping().queue()

        val json = "application/json; charset=utf-8".toMediaType()
        val body = """{
                "background": "messhall",
                "character": "knox",
                "text": "${args.joinToString(" ").replace("\"", "\\\"")}"
            }""".trimIndent().toRequestBody(json)

        val request = Request.Builder()
            .url("https://kyuu.to/dialog")
            .post(body)
            .build()

        moveThisToAStorageLocation.newCall(request).enqueue(object : OkHttp3Callback {
            override fun onFailure(call: Call, e: IOException) {
                TODO("Not yet implemented")
            }

            override fun onResponse(call: Call, response: Response) {
                val stream = IOUtil.readFully(IOUtil.getBody(response))

                event.channel.sendFile(stream, "file.png")
                    .append(event.author.asMention)
                    .append(", Here ya go!")
                    .queue()
            }
        })
    }
}
