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

import com.fasterxml.jackson.core.type.TypeReference
import io.github.yuutoproject.yuutobot.commands.base.AbstractCommand
import io.github.yuutoproject.yuutobot.commands.base.CommandCategory
import io.github.yuutoproject.yuutobot.extensions.getStaticAvatarUrl
import io.github.yuutoproject.yuutobot.objects.Character
import io.github.yuutoproject.yuutobot.utils.jackson
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent

class Route : AbstractCommand("route", CommandCategory.INFO, "Tells you what route to play next", "route") {
    private val endings = listOf("perfect", "good", "bad", "worst")
    private val characters: List<Character>

    init {
        val json = jackson.readTree(this.javaClass.getResource("/routes.json"))
        characters = jackson.readValue(json.traverse(), object : TypeReference<List<Character>>() {})
    }

    override fun run(args: MutableList<String>, event: GuildMessageReceivedEvent) {
        val route = characters.random()
        val ending = endings.random()
        val message = event.message

        val messageEmbed = EmbedBuilder()
            .setAuthor(message.member!!.effectiveName, null, message.author.getStaticAvatarUrl())
            .setTitle("Next: ${route.name}, $ending ending")
            .setThumbnail(route.emoteId.asEmoteUrl())
            .setDescription(route.description)
            .addField("Age", route.age, true)
            .addField("Birthday", route.birthday, true)
            .addField("Animal Motif", route.animal, true)
            .setFooter("Play ${route.firstName}'s route next. All bois are best bois.")
            .setColor(route.color)
            .build()

        event.channel.sendMessage(messageEmbed).queue()
    }

    private fun String.asEmoteUrl() = "https://cdn.discordapp.com/emojis/$this.gif?v=1"
}
