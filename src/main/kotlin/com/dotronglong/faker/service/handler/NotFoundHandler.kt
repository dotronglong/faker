package com.dotronglong.faker.service.handler

import com.dotronglong.faker.contract.Handler
import org.springframework.http.HttpStatus
import org.springframework.http.server.reactive.ServerHttpRequest
import org.springframework.http.server.reactive.ServerHttpResponse
import reactor.core.publisher.Mono

class NotFoundHandler : Handler {
    override fun handle(request: ServerHttpRequest, response: ServerHttpResponse): Mono<Void> {
        response.statusCode = HttpStatus.NOT_FOUND
        return Mono.empty()
    }
}