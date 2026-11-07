package lesson09

import lesson09.dsl.check01
import lesson09.dsl.check02
import lesson09.dsl.check03
import lesson09.dsl.check04
import lesson09.dsl.check05
import lesson09.dsl.check06
import lesson09.legacy.check01 as legacy01
import lesson09.legacy.check02 as legacy02
import lesson09.legacy.check03 as legacy03
import lesson09.legacy.check04 as legacy04
import lesson09.legacy.check05 as legacy05
import lesson09.legacy.check06 as legacy06
import kotlin.test.Test
import kotlin.test.assertEquals

class LegacyComparisonTest {

    private fun cmp(
        legacy: (HttpResponse) -> Boolean,
        new: (HttpResponse) -> lesson09.dsl.ValidationResult,
        response: HttpResponse,
        expectedPass: Boolean
    ) {
        assertEquals(expectedPass, legacy(response), "legacy: $response")
        assertEquals(expectedPass, new(response).isSuccess, "new: $response -> ${new(response)}")
    }

    private val j = listOf("application/json")

    @Test
    fun check01() {
        cmp(::legacy01, ::check01, HttpResponse(204, emptyMap(), null), true)
        cmp(::legacy01, ::check01, HttpResponse(204, emptyMap(), ""), true)
        cmp(::legacy01, ::check01, HttpResponse(200, emptyMap(), null), false)
        cmp(::legacy01, ::check01, HttpResponse(204, emptyMap(), " "), false)
    }

    @Test
    fun check02() {
        cmp(::legacy02, ::check02, HttpResponse(200, mapOf("Content-Type" to j), "x"), true)
        cmp(::legacy02, ::check02, HttpResponse(200, mapOf("Content-Type" to j), "not json"), true)
        cmp(::legacy02, ::check02, HttpResponse(200, mapOf("Content-Type" to j), null), false)
        cmp(::legacy02, ::check02, HttpResponse(200, mapOf("Content-Type" to j), ""), false)
        cmp(::legacy02, ::check02, HttpResponse(200, emptyMap(), "x"), false)
        cmp(::legacy02, ::check02, HttpResponse(500, mapOf("Content-Type" to j), "x"), false)
        cmp(::legacy02, ::check02, HttpResponse(200, mapOf("Content-Type" to listOf("application/jsonp")), "x"), true)
    }

    @Test
    fun check03() {
        cmp(::legacy03, ::check03,
            HttpResponse(200, mapOf("Accept" to listOf("application/json", "text/html")), null), true)
        cmp(::legacy03, ::check03,
            HttpResponse(200, mapOf("Accept" to listOf("application/json,text/html")), null), false)
        cmp(::legacy03, ::check03,
            HttpResponse(200, mapOf("Accept" to listOf("application/json")), null), false)
        cmp(::legacy03, ::check03,
            HttpResponse(500, mapOf("Accept" to listOf("application/json", "text/html")), null), false)
    }

    @Test
    fun check04() {
        cmp(::legacy04, ::check04,
            HttpResponse(200, emptyMap(), """{"id":null,"name":"Ivanov","extra":1}"""), true)
        cmp(::legacy04, ::check04,
            HttpResponse(200, emptyMap(), """{"name":"Ivanov"}"""), false)
        cmp(::legacy04, ::check04,
            HttpResponse(200, emptyMap(), """{"id":1,"name":"Petrov"}"""), false)
        cmp(::legacy04, ::check04,
            HttpResponse(200, emptyMap(), "{}"), false)
        cmp(::legacy04, ::check04,
            HttpResponse(200, emptyMap(), "not json"), false)
        cmp(::legacy04, ::check04,
            HttpResponse(200, emptyMap(), null), false)
    }

    @Test
    fun check05() {
        cmp(::legacy05, ::check05,
            HttpResponse(200, emptyMap(), """{"id":42,"active":true}"""), true)
        cmp(::legacy05, ::check05,
            HttpResponse(200, emptyMap(), """{"id":"42","active":true}"""), false)
        cmp(::legacy05, ::check05,
            HttpResponse(200, emptyMap(), """{"id":42,"active":"true"}"""), false)
        cmp(::legacy05, ::check05,
            HttpResponse(200, emptyMap(), """{"id":42}"""), false)
    }

    @Test
    fun check06() {
        cmp(::legacy06, ::check06,
            HttpResponse(422, mapOf("Content-Type" to j), """{"error":"invalid","retry":false}"""), true)
        cmp(::legacy06, ::check06,
            HttpResponse(200, mapOf("Content-Type" to j), """{"error":"invalid","retry":false}"""), false)
        cmp(::legacy06, ::check06,
            HttpResponse(422, emptyMap(), """{"error":"invalid","retry":false}"""), false)
        cmp(::legacy06, ::check06,
            HttpResponse(422, mapOf("Content-Type" to j), """{"error":"bad","retry":false}"""), false)
        cmp(::legacy06, ::check06,
            HttpResponse(422, mapOf("Content-Type" to j), """{"error":"invalid","retry":true}"""), false)
        cmp(::legacy06, ::check06,
            HttpResponse(422, mapOf("Content-Type" to j), "not json"), false)
    }
}