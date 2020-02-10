package com.dotronglong.faker.service.plugin

import com.dotronglong.faker.contract.Plugin
import com.dotronglong.faker.pojo.MutableResponse
import reactor.core.publisher.Mono

class CorsResponsePlugin : Plugin {
    override val name: String
        get() = "cors"

    override fun run(response: MutableResponse, parameters: Any): Mono<Void> {
        return Mono.create { s ->
            response.headers["Access-Control-Allow-Credentials"] = "true"
            response.headers["Access-Control-Allow-Origin"] = "*"
            response.headers["Access-Control-Expose-Headers"] = "Authorization, Access-Control-Allow-Origin, Access-Control-Allow-Credentials"
            response.headers["Access-Control-Allow-Methods"] = "GET, POST, PUT, PATCH, DELETE, OPTIONS"
            s.success()
        }
    }
}