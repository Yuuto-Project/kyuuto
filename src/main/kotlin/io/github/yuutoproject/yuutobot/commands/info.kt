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
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent

class Info : AbstractCommand("info", CommandCategory.INFO, "Shows the information about the bot and it's developers", "info") {

    override fun run(args: MutableList<String>, event: GuildMessageReceivedEvent) {
        val InfoEmbed = EmbedBuilder()
            .setColor(0xFF93CE)
            .setAuthor(
                "Yuuto from Camp Buddy",
                "https://blitsgames.com",
                "https://cdn.discordapp.com/emojis/593518771554091011.png"
            )
            .setDescription(
                "Yuuto was made and developed by the community, for the community. \n" +
                    "Join the dev team and start developing on the [project website](https://kyuuto.io/docs). \n\n" +
                    "Version 3.0 (Kyuuto v1) was made and developed by: \n" +
                    "**Arch#0226**, **dunste123#0129**, **Tai Chi#4634**, **zsotroav#8941** \n \n" +
                    "Quick Change log: \n" +
                    "```diff\nMoved from JavaScript to Kotlin \n```"
            )
            .setFooter("Yuuto: Release 3.0 | 2020-TBA")

        event.channel.sendMessage(InfoEmbed.build()).queue()
    }
}
