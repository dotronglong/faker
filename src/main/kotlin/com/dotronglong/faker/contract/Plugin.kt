package com.dotronglong.faker.contract

import com.dotronglong.faker.pojo.Spec
import org.springframework.http.server.reactive.ServerHttpResponse
import reactor.core.publisher.Mono

interface Plugin {
    val name: String
    fun run(spec: Spec.Response, response: ServerHttpResponse, parameters: Any): Mono<Void>
}