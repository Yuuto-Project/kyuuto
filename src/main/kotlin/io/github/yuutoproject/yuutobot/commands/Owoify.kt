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
import io.github.yuutoproject.yuutobot.utils.httpClient
import io.github.yuutoproject.yuutobot.utils.jackson
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent
import net.dv8tion.jda.api.utils.MarkdownSanitizer
import okhttp3.Request
import java.io.File
import java.util.*

class Owoify : AbstractCommand(
    "owoify",
    CommandCategory.FUN,
    // TODO: Change this description
    "Yuuto can turn owoify your text! (whatever that means)",
    "owoify [level] <text>"
) {
    private val levels = listOf("easy", "medium", "hard")

    init {
        downloadFile()
    }

    override fun run(args: MutableList<String>, event: GuildMessageReceivedEvent) {
        if (args.size == 0) {
            event.channel.sendMessage("Sorry, but you need to provide me a message to owoify").queue()
            return
        }

        val level = if (levels.contains(args[0].toLowerCase())) {
            when (args.removeAt(0).toLowerCase()) {
                "hard" -> "uvu"
                "medium" -> "uwu"
                else -> "owo"
            }
        } else {
            "owo"
        }

        val input = args.joinToString(" ")

        if (input.length > 1000) {
            event.channel.sendMessage("Sorry, but the character limit is 1000~!").queue()
            return
        }

        val converted = MarkdownSanitizer.escape(runOwo(level, input).replace("`", "\\`"))

        event.channel.sendMessage(
            "OwO-ified for ${event.author.asMention}~!\n\n$converted"
        ).queue()
    }

    private fun runOwo(level: String, message: String): String {
        ProcessBuilder("node", FILE_NAME, level, message)
            .start()
            .inputStream.use { s ->
                Scanner(s).use { scanner ->
                    return buildString {
                        while (scanner.hasNextLine()) {
                            appendln(scanner.nextLine())
                        }
                    }
                }
            }
    }

    private fun downloadFile() {
        val file = File(FILE_NAME)

        if (file.exists()) {
            logger.debug("File exists")
            return
        }

        val findRelease = Request.Builder()
            .applyDefaults()
            .url("https://api.github.com/repos/Yuuto-Project/owo-cli/releases/latest")
            .build()

        httpClient.newCall(findRelease).execute().use { res ->
            val json = jackson.readTree(res.body!!.byteStream())
            val downLoadUrl = json["assets"][0]["browser_download_url"].asText()

            logger.info("Downloading owoify from $downLoadUrl")

            val download = Request.Builder()
                .applyDefaults()
                .url(downLoadUrl)
                .build()

            httpClient.newCall(download).execute().use { response ->
                file.writer().use {
                    it.write(response.body!!.string())
                }
            }
        }
    }

    companion object {
        const val FILE_NAME = "owoify.bundle.js"
    }
}
