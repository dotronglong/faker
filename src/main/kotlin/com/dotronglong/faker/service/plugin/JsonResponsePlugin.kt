package com.dotronglong.faker.service.plugin

import com.dotronglong.faker.contract.Plugin
import com.dotronglong.faker.pojo.Spec
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import org.springframework.http.HttpStatus
import org.springframework.http.server.reactive.ServerHttpResponse
import reactor.core.publisher.Mono

class JsonResponsePlugin : Plugin {
    private val mapper = ObjectMapper()

    private companion object {
        const val CONTENT_JSON_UTF8 = "application/json; charset=utf-8"
    }

    init {
        mapper.registerKotlinModule()
    }

    override val name: String
        get() = "json"

    override fun run(spec: Spec.Response, response: ServerHttpResponse, parameters: Any): Mono<Void> {
        response.headers.set("Content-Type", CONTENT_JSON_UTF8)
        if (spec.statusCode != null) {
            response.statusCode = HttpStatus.valueOf(spec.statusCode!!)
        }
        if (spec.headers != null) {
            spec.headers!!.forEach { (key, value) -> response.headers.set(key, value) }
        }

        var body = spec.body
        if (body == null) {
            body = Any()
        }
        val bytes = mapper.writeValueAsBytes(body)
        val buffer = response.bufferFactory().wrap(bytes)
        return response.writeAndFlushWith(Mono.just(Mono.just(buffer)))
    }
}