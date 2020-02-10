package com.dotronglong.faker.contract

import com.dotronglong.faker.pojo.MutableResponse
import reactor.core.publisher.Mono

interface Plugin {
    val name: String
    fun run(response: MutableResponse, parameters: Any): Mono<Void>
}