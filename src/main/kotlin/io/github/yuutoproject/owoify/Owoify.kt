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

package io.github.yuutoproject.owoify

enum class OwoifyLevel{
    Owo, Uwu, Uvu
}

val wordRegex = """[^\s]+""".toRegex()
val spaceRegex = """\s+""".toRegex()

fun owoify(text: String, level:OwoifyLevel) : String{
    val wordMatches = wordRegex.findAll(text)
    val spaceRegex = spaceRegex.findAll(text)

    var words = wordMatches.map { Word(it.value) }
    val spaces = spaceRegex.map{ Word(it.value) }

    words.forEach { println(it.word) }

    words = words.map{
        
    }

    when(level){
        OwoifyLevel.Owo->{

        }
        OwoifyLevel.Uwu ->{

        }
        OwoifyLevel.Uvu ->{

        }
    }

    //wordMatches.forEach { println(it.value) }
    //spaceRegex.forEach { println( it.value) }

    return text;
}
