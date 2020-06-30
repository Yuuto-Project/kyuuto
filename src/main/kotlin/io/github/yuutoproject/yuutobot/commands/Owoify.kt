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
import io.github.yuutoproject.yuutobot.utils.Constants
import io.github.yuutoproject.yuutobot.utils.httpClient
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent
import okhttp3.Call
import okhttp3.Request
import okhttp3.Response
import java.io.File
import java.io.IOException
import java.util.*
import okhttp3.Callback as OkHttp3Callback

class Owoify : AbstractCommand(
    "owoify",
    CommandCategory.FUN,
    // TODO: Change this description
    "Yuuto can turn owoify your text! (whatever that means)",
    "owoify <text>"
) {
    private val levels = listOf("uwu", "uvu", "owo")

    init {
        downloadFile()
    }

    override fun run(args: MutableList<String>, event: GuildMessageReceivedEvent) {
        if (args.size == 0) {
            event.channel.sendMessage("Sorry, but you need to provide me a message to owoify").queue()
            return
        }

        val level = args.removeAt(0).toLowerCase()

        if (!levels.contains(level)) {
            return
        }

        val converted = runOwo(level, args.joinToString(" "))

        event.channel.sendMessage(converted).queue()
    }

    private fun runOwo(level: String, message: String): String {
        ProcessBuilder("node", FILE_NAME, level, message)
            .start()
            .inputStream.use { s ->
                Scanner(s).use { scanner ->
                    val output = buildString {
                        while (scanner.hasNextLine()) {
                            appendln(scanner.nextLine())
                        }
                    }

                    println(output)

                    return output
                }
            }
    }

    private fun downloadFile() {
        val file = File(FILE_NAME)

        if (file.exists()) {
            println("File exists")
            return
        }

        val request = Request.Builder()
            .url("https://raw.githubusercontent.com/Yuuto-Project/owo-cli/master/dist/$FILE_NAME?r=${System.currentTimeMillis()}")
            .header(
                "User-Agent",
                "Yuuto Discord Bot / ${Constants.YUUTO_VERSION} https://github.com/Yuuto-Project/kyuuto"
            )
            .get()
            .build()

        httpClient.newCall(request).enqueue(object : OkHttp3Callback {
            override fun onFailure(call: Call, e: IOException) {
                e.printStackTrace()
            }

            override fun onResponse(call: Call, response: Response) {
                println("Downloaded file")
                file.writer().use {
                    it.write(response.body!!.string())
                }
            }
        })
    }

    companion object {
        const val FILE_NAME = "owoify.bundle.js"
    }
}
