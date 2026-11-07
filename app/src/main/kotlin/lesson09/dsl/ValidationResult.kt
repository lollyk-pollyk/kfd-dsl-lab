package lesson09.dsl

data class Deviation(
    val path: String,
    val expected: String,
    val actual: String
)


data class ValidationResult(val deviations: List<Deviation>) {
    val isSuccess: Boolean get() = deviations.isEmpty()

    override fun toString(): String =
        if (isSuccess) "ValidationResult(success)"
        else "ValidationResult(deviations=\n  ${deviations.joinToString("\n  ")}\n)"
}