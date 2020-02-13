package com.dotronglong.faker.service.plugin

import com.dotronglong.faker.contract.Plugin
import com.dotronglong.faker.pojo.MutableResponse
import org.springframework.http.HttpStatus
import org.springframework.http.server.reactive.ServerHttpResponse
import reactor.core.publisher.Mono

class JsonResponsePlugin constructor(private val serverHttpResponse: ServerHttpResponse) : Plugin {
    private companion object {
        const val CONTENT_JSON_UTF8 = "application/json; charset=utf-8"
        const val HEADER_CONTENT_TYPE = "Content-Type"
    }

    override val name: String
        get() = "json"

    override fun run(response: MutableResponse, arguments: Map<String, Any>?): Mono<Void> {
        return Mono.create { s ->
            serverHttpResponse.statusCode = HttpStatus.valueOf(response.statusCode)
            if (!response.headers.containsKey(HEADER_CONTENT_TYPE)) {
                serverHttpResponse.headers.set(HEADER_CONTENT_TYPE, CONTENT_JSON_UTF8)
            }
            response.headers.forEach { (key, value) -> serverHttpResponse.headers.set(key, value) }
            val buffer = serverHttpResponse.bufferFactory().wrap(response.body.toByteArray())
            serverHttpResponse.writeAndFlushWith(Mono.just(Mono.just(buffer))).subscribe()
            s.success()
        }
    }
}