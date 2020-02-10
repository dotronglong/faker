package com.dotronglong.faker.contract

import org.springframework.http.server.reactive.ServerHttpRequest
import org.springframework.http.server.reactive.ServerHttpResponse
import reactor.core.publisher.Mono

interface Handler {
    fun handle(request: ServerHttpRequest, response: ServerHttpResponse): Mono<Void>
}