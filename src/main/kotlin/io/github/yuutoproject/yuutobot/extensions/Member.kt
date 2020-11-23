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

package io.github.yuutoproject.yuutobot.extensions

import io.github.yuutoproject.yuutobot.utils.Constants.DEV_ROLE_ID
import io.github.yuutoproject.yuutobot.utils.Constants.KYUUTO_GUILD
import net.dv8tion.jda.api.entities.Member

fun Member.getStaticAvatarUrl(size: Int = 128): String {
    return this.user.getStaticAvatarUrl(size)
}

val Member.isDeveloper: Boolean
    get() {
        val guild = this.jda.getGuildById(KYUUTO_GUILD) ?: return false
        val member = guild.getMemberById(this.idLong) ?: return false

        return member.roles.contains(guild.getRoleById(DEV_ROLE_ID)!!)
    }
