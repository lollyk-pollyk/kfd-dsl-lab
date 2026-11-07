package lesson09.dsl

import lesson09.HttpResponse
import lesson09.support.json.JsonParseResult
import lesson09.support.json.parseJsonObject



@HttpValidationDsl
class ResponseValidator internal constructor(
    private val response: HttpResponse,
    private val collector: Collector
) {
    fun status(block: StatusValidator.() -> Unit) {
        StatusValidator(response.status, collector).block()
    }

    fun header(name: String, block: HeaderValidator.() -> Unit) {
        HeaderValidator(name, response.headers, collector).block()
    }

    fun body(block: BodyValidator.() -> Unit) {
        BodyValidator(response.body, collector).block()
    }
}



@HttpValidationDsl
class StatusValidator internal constructor(
    private val actual: Int,
    private val collector: Collector
) {
    fun equalTo(expected: Int) {
        if (actual != expected) {
            collector.add("status", "expected $expected", "actual $actual")
        }
    }
}



@HttpValidationDsl
class HeaderValidator internal constructor(
    private val name: String,
    private val headers: Map<String, List<String>>,
    private val collector: Collector
) {
    private val path = "headers.$name"

    /** Values collected from all keys matching [name] ignoring case. */
    private val values: List<String>? = run {
        val present = headers.keys.any { it.equals(name, ignoreCase = true) }
        if (!present) null
        else headers.entries
            .filter { it.key.equals(name, ignoreCase = true) }
            .flatMap { it.value }
    }

    private fun describeActual(): String = when (values) {
        null -> "<missing>"
        else -> values.joinToString(prefix = "[", postfix = "]")
    }

    private fun anyValue(predicate: (String) -> Boolean): Boolean =
        values != null && values.any(predicate)

    fun equalTo(expected: String) {
        if (!anyValue { it == expected }) {
            collector.add(path, "expected value $expected", describeActual())
        }
    }

    fun startsWith(prefix: String) {
        if (!anyValue { it.startsWith(prefix) }) {
            collector.add(path, "expected prefix $prefix", describeActual())
        }
    }

    fun satisfies(description: String, predicate: (String) -> Boolean) {
        if (!anyValue(predicate)) {
            collector.add(path, "expected $description", describeActual())
        }
    }
}



@HttpValidationDsl
class BodyValidator internal constructor(
    private val body: String?,
    private val collector: Collector
) {
    fun absentOrEmpty() {
        if (!(body == null || body == "")) {
            collector.add("body", "expected absent or empty", "actual ${quote(body)}")
        }
    }

    fun notEmpty() {
        if (body == null || body.isEmpty()) {
            collector.add("body", "expected non-empty", "actual ${quote(body)}")
        }
    }

    fun json(block: JsonObjectValidator.() -> Unit) {
        if (body == null) {
            collector.add("body.json", "expected valid JSON object", "actual <missing>")
            return
        }
        when (val parsed = parseJsonObject(body)) {
            is JsonParseResult.Success ->
                JsonObjectValidator(parsed.fields, collector).block()
            is JsonParseResult.Failure ->
                collector.add("body.json", "expected valid JSON object", "actual ${parsed.reason}")
        }
    }

    private fun quote(value: String?): String =
        if (value == null) "<missing>" else "\"$value\""
}



@HttpValidationDsl
class JsonObjectValidator internal constructor(
    private val fields: Map<String, Any?>,
    private val collector: Collector
) {
    fun field(name: String, block: JsonFieldValidator.() -> Unit) {
        JsonFieldValidator(name, fields, collector).block()
    }
}



@HttpValidationDsl
class JsonFieldValidator internal constructor(
    private val name: String,
    private val fields: Map<String, Any?>,
    private val collector: Collector
) {
    private val path = "body.json.$name"

    private fun actualDescription(): String =
        if (!fields.containsKey(name)) "<missing>" else formatValue(fields[name])

    fun present() {
        if (!fields.containsKey(name)) {
            collector.add(path, "expected present", "actual <missing>")
        }
    }

    fun equalTo(expected: String) = checkEquals(expected, "string")
    fun equalTo(expected: Long) = checkEquals(expected, "long")
    fun equalTo(expected: Boolean) = checkEquals(expected, "boolean")

    private fun checkEquals(expected: Any, typeName: String) {
        if (!fields.containsKey(name) || fields[name] != expected) {
            collector.add(path, "expected $typeName ${formatValue(expected)}", actualDescription())
        }
    }

    private fun formatValue(value: Any?): String = when (value) {
        null -> "null"
        is String -> "\"$value\""
        else -> value.toString()
    }
}