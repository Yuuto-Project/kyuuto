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

import io.github.yuutoproject.owoify.OwoifyLevel
import io.github.yuutoproject.owoify.owoify
import io.github.yuutoproject.yuutobot.commands.base.AbstractCommand
import io.github.yuutoproject.yuutobot.commands.base.CommandCategory
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent

class Owoify : AbstractCommand(
    "owoify",
    CommandCategory.FUN,
    //TODO: Change this description
    "Yuuto can turn owoify your text! (whatever that means)",
    "owoify <text>"
) {

    override fun run(args: MutableList<String>, event: GuildMessageReceivedEvent) {
        if(args.size == 0){
            event.channel.sendMessage("Sorry, but you need to provide me a message to owoify").queue()
            return;
        }

        val difficulty = when(args[0].toLowerCase()){
            "uwu" -> OwoifyLevel.Uwu
            "uvu" -> OwoifyLevel.Uvu
            else ->  OwoifyLevel.Owo
        }

        val owoifiedText = owoify(args.joinToString(" "), difficulty)
        event.channel.sendMessage(owoifiedText).queue()
    }
}
