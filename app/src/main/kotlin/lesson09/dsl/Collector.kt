package lesson09.dsl

internal class Collector {
    private val items = mutableListOf<Deviation>()

    fun add(path: String, expected: String, actual: String) {
        items += Deviation(path, expected, actual)
    }

    fun toResult(): ValidationResult = ValidationResult(items.toList())
}