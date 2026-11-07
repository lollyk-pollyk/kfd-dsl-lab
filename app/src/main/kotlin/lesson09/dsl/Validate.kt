package lesson09.dsl

import lesson09.HttpResponse

fun validate(response: HttpResponse, block: ResponseValidator.() -> Unit): ValidationResult {
    val collector = Collector()
    ResponseValidator(response, collector).block()
    return collector.toResult()
}