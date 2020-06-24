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

class Law : AbstractCommand("law", CommandCategory.INFO, "Shows the buddy law", "law") {
    override val aliases = arrayOf("buddylaw")

    override fun run(args: MutableList<String>, event: GuildMessageReceivedEvent) {
        val lawEmbed = EmbedBuilder()
            .setColor(0xFF93CE)
            .setTitle("The Buddy Law")
            .setDescription(
                """
                |1) A buddy should be kind, helpful and trustworthy to each other!
                |2) A buddy must be always ready for anything!
                |3) A buddy should always show a bright smile on his face!
                |||4) We leave no buddy behind!||
                """.trimMargin()
            )

        event.channel.sendMessage(lawEmbed.build()).queue()
    }
}
