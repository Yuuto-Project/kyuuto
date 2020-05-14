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
import io.github.yuutoproject.yuutobot.objects.Character
import java.lang.IllegalArgumentException
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent
import net.dv8tion.jda.api.utils.data.DataArray

@Suppress("UNCHECKED_CAST")
class Route : AbstractCommand("route", CommandCategory.INFO, "Tells you what route to play next", "route") {
    private val endings = listOf("perfect", "good", "bad", "worst")

    private val routes: List<Character>

    init {
        routes = DataArray.fromJson(this.javaClass.getResource("/routes.json").readText())
            .map {
                if (it !is HashMap<*, *>) {
                    throw IllegalArgumentException("A")
                }
                Character(it as HashMap<String, Any>)
            }
    }

    override fun run(args: MutableList<String>, event: GuildMessageReceivedEvent) {
        val route = routes.random()
        val ending = endings.random()

        val name = route.name
        val firstName = name.split(" ")[0]

        val message = event.message

        val messageEmbed = EmbedBuilder()
            .setAuthor(message.member!!.nickname, null, message.author.avatarUrl)
            .setTitle("Next: $name, $ending ending")
            .setThumbnail(getEmoteUrl(route.emoteId))
            .setDescription(route.description)
            .addField("Age", route.age, true)
            .addField("Birthday", route.birthday, true)
            .addField("Animal Motif", route.animal, true)
            .setFooter("Play $firstName's route next. All bois are best bois.")
            .setColor(route.color)
            .build()

        event.channel.sendMessage(messageEmbed).queue()
    }

    private fun getEmoteUrl(emoteId: String) = "https://cdn.discordapp.com/emojis/$emoteId.gif?v=1"
}
