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

package io.github.yuutoproject.yuutobot

import io.github.cdimascio.dotenv.Dotenv
import net.dv8tion.jda.api.GatewayEncoding
import net.dv8tion.jda.api.JDABuilder
import net.dv8tion.jda.api.entities.Activity
import net.dv8tion.jda.api.requests.GatewayIntent
import net.dv8tion.jda.api.utils.ChunkingFilter
import net.dv8tion.jda.api.utils.MemberCachePolicy
import net.dv8tion.jda.api.utils.cache.CacheFlag.*
import org.slf4j.LoggerFactory

class Yuuto {
    private val logger = LoggerFactory.getLogger(this.javaClass)

    init {
        // Print something with the logger
        // You can also use println but that does not look as nice
        logger.info("Booting YuutoKt")
        logger.info("Prefix is {}", config["PREFIX"])

        JDABuilder.createDefault(
            config["TOKEN"],
            GatewayIntent.GUILD_MEMBERS,
            GatewayIntent.GUILD_MESSAGES,
            GatewayIntent.GUILD_MESSAGE_REACTIONS
        )
            .addEventListeners(Listener())
            .disableCache(ACTIVITY, CLIENT_STATUS, EMOTE, VOICE_STATE)
            .setChunkingFilter(ChunkingFilter.ALL)
            .setMemberCachePolicy(MemberCachePolicy.ALL)
            .setActivity(Activity.playing("volleyball"))
            .setBulkDeleteSplittingEnabled(false)
            // ETF is more optimized
            .setGatewayEncoding(GatewayEncoding.ETF)
            .build()
    }

    companion object {
        val config = Dotenv.load()
    }
}

fun main() {
    Yuuto()
}
