package com.dotronglong.faker.service.plugin

import com.dotronglong.faker.contract.Plugin
import com.dotronglong.faker.pojo.MutableResponse
import org.springframework.http.HttpStatus
import org.springframework.http.server.reactive.ServerHttpResponse
import reactor.core.publisher.Mono

class JsonResponsePlugin constructor(private val serverHttpResponse: ServerHttpResponse) : Plugin {
    private companion object {
        const val CONTENT_JSON_UTF8 = "application/json; charset=utf-8"
    }

    override val name: String
        get() = "json"

    override fun run(response: MutableResponse, parameters: Any): Mono<Void> {
        serverHttpResponse.statusCode = HttpStatus.valueOf(response.statusCode)
        if (!response.headers.containsKey("Content-Type")) {
            serverHttpResponse.headers.set("Content-Type", CONTENT_JSON_UTF8)
        }
        response.headers.forEach { (key, value) -> serverHttpResponse.headers.set(key, value) }
        val buffer = serverHttpResponse.bufferFactory().wrap(response.body.toByteArray())
        return serverHttpResponse.writeAndFlushWith(Mono.just(Mono.just(buffer)))
    }
}