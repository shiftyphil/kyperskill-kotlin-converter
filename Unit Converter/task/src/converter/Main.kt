package converter

import java.util.Scanner

class NegativeValue(message: String) : Throwable(message)
class InvalidConversion : Throwable()

enum class MeasureType(val allowNegative: Boolean, val baseUnit: Measure, val description: String) {
    Length(false, Measure.M, "Length"),
    Mass(false, Measure.KG, "Weight"),
    Temperature(true, Measure.K, "Temperature"),
    NULL(true, Measure.NULL, "???");
}

enum class Measure(val abbreviation: String, val singular: String, val plural: String, val aliases: List<String>?,
                   val type: MeasureType, val ratio: Double, val offset: Double) {
    M("m", "meter", "meters", null, MeasureType.Length, 1.0, 0.0),
    KM("km", "kilometer", "kilometers", null, MeasureType.Length, 1000.0, 0.0),
    CM("cm", "centimeter", "centimeters", null, MeasureType.Length, 0.01, 0.0),
    MM("mm", "millimeter", "millimeters", null, MeasureType.Length, 0.001, 0.0),
    MI("mi", "mile", "miles", null, MeasureType.Length, 1609.35, 0.0),
    YD("yd", "yard", "yards", null, MeasureType.Length, 0.9144, 0.0),
    FT("ft", "foot", "feet", null, MeasureType.Length, 0.3048, 0.0),
    IN("in", "inch", "inches", null, MeasureType.Length, 0.0254, 0.0),
    G("g", "gram", "grams", null, MeasureType.Mass, 1.0, 0.0),
    KG("kg", "kilogram", "kilograms", null, MeasureType.Mass, 1000.0, 0.0),
    MG("mg", "milligram", "milligrams", null, MeasureType.Mass, 0.001, 0.0),
    LB("lb", "pound", "pounds", null, MeasureType.Mass, 453.592, 0.0),
    OZ("oz", "ounce", "ounces", null, MeasureType.Mass, 28.3495, 0.0),
    K("K", "Kelvin", "Kelvins", null, MeasureType.Temperature, 1.0, 0.0),
    C("C", "degree Celsius", "degrees Celsius", listOf("celsius", "dc"), MeasureType.Temperature, 1.0, 273.15),
    F("F", "degree Fahrenheit", "degrees Fahrenheit", listOf("fahrenheit", "df"), MeasureType.Temperature, 5.0 / 9.0, 459.67),
    NULL("err", "???", "???", null, MeasureType.NULL, 0.0, 0.0);

    fun convertTo(x: Double, target: Measure): Double {
        if (x < 0.0 && !this.type.allowNegative) {
            throw NegativeValue("${type.description} shouldn't be negative")
        }
        return when {
            type.baseUnit == target -> (x + offset) * ratio
            target.type.baseUnit == this -> x / target.ratio - target.offset
            this != NULL && type == target.type -> (x + offset) * ratio / target.ratio - target.offset
            else -> throw InvalidConversion()
        }
    }

    fun getNameFor(x: Double): String {
        return if (x == 1.0) singular else plural
    }

    companion object {
        fun findByName(name: String): Measure {
            val search = name.toUpperCase()
            return values().firstOrNull {
                it.abbreviation.toUpperCase() == search
                        || it.singular.toUpperCase() == search
                        || it.plural.toUpperCase() == search
                        || it.aliases?.any { alias -> alias.toUpperCase() == search } ?: false
            } ?: NULL
        }
    }
}

fun main() {
    val scanner = Scanner(System.`in`)
    while (true) {
        print("Enter what you want to convert (or exit): ")
        val line = scanner.findInLine("([\\d\\.-]+)\\h+(\\b[\\w\\h]+\\b)\\h+(?:in|to)\\h+(\\b[\\w\\h]+\\b)")
        if (line != null) {
            val line = scanner.match()
            scanner.nextLine()

            val x = line.group(1).toDouble()
            val sourceUnit = Measure.findByName(line.group(2))
            val targetUnit = Measure.findByName(line.group(3))

            try {
                val result = sourceUnit.convertTo(x, targetUnit)
                println("$x ${sourceUnit.getNameFor(x)} is $result ${targetUnit.getNameFor(result)}")
            } catch (ex: InvalidConversion) {
                println("Conversion from ${sourceUnit.plural} to ${targetUnit.plural} is impossible")
            } catch (ex: Throwable) {
                println(ex.message)
            }
        } else {
            when (scanner.nextLine()) {
                "exit" -> return
                else -> println("Conversion from ??? to ??? is impossible.")
            }
        }
    }
}
