package com.dotronglong.faker.contract

import com.dotronglong.faker.pojo.MutableResponse
import reactor.core.publisher.Mono

interface Plugin {
    val name: String
    fun run(response: MutableResponse, arguments: Map<String, Any>?): Mono<Void>
    fun run(response: MutableResponse): Mono<Void> = run(response, null)
}