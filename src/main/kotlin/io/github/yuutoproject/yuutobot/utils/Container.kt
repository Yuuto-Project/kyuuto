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

package io.github.yuutoproject.yuutobot.utils

import com.fasterxml.jackson.core.json.JsonReadFeature
import com.fasterxml.jackson.databind.json.JsonMapper
import okhttp3.OkHttpClient

val httpClient = OkHttpClient()

// Special jackson configuration to allow for json and json5 (partially) loading
val jackson = JsonMapper.builder()
    .enable(
        JsonReadFeature.ALLOW_TRAILING_COMMA,
        JsonReadFeature.ALLOW_JAVA_COMMENTS,
        JsonReadFeature.ALLOW_YAML_COMMENTS,
        JsonReadFeature.ALLOW_UNQUOTED_FIELD_NAMES
    )
    .build()
