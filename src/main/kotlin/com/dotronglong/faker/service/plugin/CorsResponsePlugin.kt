package com.dotronglong.faker.service.plugin

import com.dotronglong.faker.contract.Plugin
import com.dotronglong.faker.pojo.MutableResponse
import reactor.core.publisher.Mono

class CorsResponsePlugin : Plugin {
    companion object {
        val HEADERS = HashMap(mapOf(
                "Access-Control-Allow-Credentials" to "true",
                "Access-Control-Allow-Origin" to "*",
                "Access-Control-Expose-Headers" to "Authorization, Access-Control-Allow-Origin, Access-Control-Allow-Credentials",
                "Access-Control-Allow-Methods" to "GET, POST, PUT, PATCH, DELETE, OPTIONS",
                "Access-Control-Allow-Headers" to "Authorization, Content-Type",
                "Access-Control-Max-Age" to "86400"
        ))
    }

    override val name: String
        get() = "cors"

    override fun run(response: MutableResponse, parameters: Any): Mono<Void> {
        return Mono.create { s ->
            response.headers.putAll(HEADERS)
            s.success()
        }
    }
}