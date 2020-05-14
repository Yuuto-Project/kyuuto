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
import net.dv8tion.jda.api.utils.data.DataArray
import net.dv8tion.jda.api.utils.data.DataObject
import java.awt.Color
import kotlin.random.Random.Default.nextInt

class Route : AbstractCommand("route", CommandCategory.INFO, "Tells you what route to play next", "route") {
    fun DataArray.random() : DataObject = getObject(nextInt(length()))

    private val endings = listOf("perfect", "good", "bad", "worst")

    private val routesJsonString = this.javaClass.getResource("/routes.json").readText()
    private val routes = DataArray.fromJson(routesJsonString)

    override fun run(args: MutableList<String>, event: GuildMessageReceivedEvent) {
        val route = routes.random()
        val ending = endings.random()

        val list = listOf("aaa");
        list.random()

        val name = route.getString("name")
        val firstName = name.split(" ")[0]

        val messageEmbed = EmbedBuilder()
            .setThumbnail(getEmoteUrl(route.getString("emoteId")))
            .setColor(Color.decode(route.getString("color")))
            .setTitle("Next : ${route.getString("name")}, $ending ending")
            .setDescription(route.getString("description"))
            .addField("Age", route.getString("age"), true)
            .addField("Birthday", route.getString("birthday"), true)
            .addField("Animal Motif", route.getString("animal"), true)
            .setFooter("Play $firstName's route next. All bois are best bois.")
            .build()

        event.channel.sendMessage(messageEmbed).queue();
    }

    fun getEmoteUrl(emoteId : String) = "https://cdn.discordapp.com/emojis/$emoteId.gif?v=1"
}
