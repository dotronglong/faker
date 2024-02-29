package com.dotronglong.faker.service.plugin

import com.dotronglong.faker.contract.Plugin
import com.dotronglong.faker.pojo.MutableResponse
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.springframework.http.server.reactive.ServerHttpRequest
import reactor.core.publisher.Mono

class DelayResponsePlugin : Plugin {
    override val name: String
        get() = "delay"

    override fun run(request: ServerHttpRequest, response: MutableResponse, arguments: Map<String, Any>?): Mono<Void> {
        return Mono.create { s ->
            val duration = (arguments?.get("duration") as Int?) ?: 0
            if (duration > 0) {
                runBlocking {
                    delay((duration).toLong())
                    s.success()
                }
            } else {
                throw Exception("Delay must be an integer and greater than zero")
            }
        }
    }
}