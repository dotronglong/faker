package com.dotronglong.faker.contract

import com.dotronglong.faker.pojo.MutableResponse
import reactor.core.publisher.Mono
import org.springframework.http.server.reactive.ServerHttpRequest

interface Plugin {
    val name: String
    fun run(request: ServerHttpRequest, response: MutableResponse, arguments: Map<String, Any>?): Mono<Void>
    fun run(request: ServerHttpRequest, response: MutableResponse): Mono<Void> = run(request, response, null)
}