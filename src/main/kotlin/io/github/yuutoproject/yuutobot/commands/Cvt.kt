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
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent
import javax.measure.Measure
import javax.measure.unit.NonSI.*
import javax.measure.unit.SI.*
import kotlin.math.pow

class Cvt : AbstractCommand(
    "cvt",
    CommandCategory.UTIL,
    "Helps converting stuff",
    "Run `cvt <target unit> <value><origin unit>` to convert `<value>` from `<origin unit>` to `<target unit>`."
) {
    override val aliases = arrayOf("convert")

    private val inputPattern = "(-?[\\d.]+)(\\D{1,3})".toRegex()
    private val lengths = arrayOf("mm", "cm", "m", "pc", "pt", "in", "ft", "px")
    private val temps = arrayOf("c", "f", "k")
    private val weights = arrayOf("kg", "lbs")

    // The + sign combines the arrays
    private val validUnits = temps + lengths + weights

    override fun run(args: MutableList<String>, event: GuildMessageReceivedEvent) {
        val channel = event.channel

        if (args.isNotEmpty() && args[0].toLowerCase() == "handegg") {
            channel.sendMessage("Americans call the handegg a football.").queue()
            return
        }

        if (args.size < 2) {
            channel.sendMessage(
                "Temperature units to convert to are `${temps.joinToString("`, `")}` from those values.\n" +
                    "Height units to convert to are `${lengths.joinToString("`, `")}` from those same values as well.\n" +
                    "Weight units to convert to are `${weights.joinToString("`, `")}` again from the same values.\n" +
                    "The syntax is `cvt <unit-to-convert-to> <value>`"
            ).queue()
            return
        }

        val targetUnit = args[0].toLowerCase()

        if (!validUnits.contains(targetUnit)) {
            channel.sendMessage("<a:ConnorShake:701311285316550656> Valid units are `${validUnits.joinToString("`, `")}`")
                .queue()
            return
        }

        val input = args[1].toLowerCase()

        if (!input.matches(inputPattern)) {
            channel.sendMessage("<:NatsumiThink:701311512714805279> Not sure what you mean by `$input`.").queue()
            return
        }

        val inputSplit = inputPattern.find(input) ?: return
        // Destructuring the list
        val (_, sourceValue, sourceUnit) = inputSplit.groupValues
        val srcUnitLower = sourceUnit.toLowerCase()

        if (!srcUnitLower.isCompatibleWithUnit(targetUnit)) {
            channel.sendMessage("<:YoichiLOL:701312070880329800> I wish that that was possible as well mate.").queue()
            return
        }

        val sourceFloat = sourceValue.toFloat()

        if ((lengths.contains(targetUnit) || weights.contains(targetUnit)) && sourceFloat < 0) {
            channel.sendMessage("<:AmaThink:701049739747000371> I don't think that `$input` is possible").queue()
            return
        }

        val converted = when {
            lengths.contains(targetUnit) -> convertLength(sourceFloat, sourceUnit, targetUnit)
            weights.contains(targetUnit) -> {
                val source = sourceUnit.toMassUnit()
                val target = targetUnit.toMassUnit()
                val converter = source.getConverterTo(target)
                val measure = Measure.valueOf(sourceFloat, source).doubleValue(source)

                converter.convert(measure).toFloat()
            }
            else -> {
                val kelvin = sourceFloat.toKelvin(srcUnitLower)

                if (kelvin < 0 || kelvin > 10F.pow(32F)) {
                    val highLow = if (kelvin < 0) "low" else "high"

                    channel.sendMessage("<:HiroOhGod:701312362401103902> Temperatures that $highLow are not possible.")
                        .queue()
                    return
                }

                kelvin.toTemp(targetUnit)
            }
        }

        val aboutPrecise = if (srcUnitLower == targetUnit) "precisely" else "about"

        channel.sendMessage(
            "<:LeeCute:701312766115315733> According to my calculations, " +
                "`$sourceFloat${srcUnitLower.displayUnit()}` is $aboutPrecise `$converted${targetUnit.displayUnit()}`"
        )
            .queue()
    }

    private fun String.isCompatibleWithUnit(unit: String): Boolean {
        return temps.contains(this) && temps.contains(unit) ||
            weights.contains(this) && weights.contains(unit) ||
            lengths.contains(this) && lengths.contains(unit)
    }

    private fun String.displayUnit() = when (this) {
        "c" -> "\u2103"
        "f" -> "\u00B0\u0046"
        "k" -> "K" // Upper case K
        else -> this
    }

    private fun Float.toKelvin(srcUnit: String) = when (srcUnit) {
        "c" -> this + 273.15F
        "f" -> (this - 32F) * (5F / 9F) + 273.15F
        "k" -> this
        else -> throw IllegalArgumentException("Invalid temperature supplied") // Should never happen
    }

    private fun Float.toTemp(targetUnit: String) = when (targetUnit) {
        "c" -> this - 273.15F
        "f" -> (this - 273.15F) * (9F / 5F) + 32F
        "k" -> this
        else -> throw IllegalArgumentException("Invalid temperature supplied") // Should never happen
    }

    private fun convertLength(num: Float, source: String, target: String): Float {
        val sourceUnit = source.toLengthUnit()
        val targetUnit = target.toLengthUnit()
        val converter = sourceUnit.getConverterTo(targetUnit)
        val measure = Measure.valueOf(num, sourceUnit).doubleValue(sourceUnit)

        return converter.convert(measure).toFloat()
    }

    private fun String.toLengthUnit() = when (this) {
        "mm" -> MILLIMETER
        "cm" -> CENTIMETER
        "m" -> METER
        "km" -> KILOMETER

        "pc" -> POINT.times(12)
        "pt" -> POINT
        "in" -> INCH
        "ft" -> FOOT
        "px" -> PIXEL

        else -> throw IllegalArgumentException("Unit not found")
    }

    private fun String.toMassUnit() = when (this) {
        "kg" -> KILOGRAM
        "lbs" -> POUND

        else -> throw IllegalArgumentException("Unit not found")
    }
}
