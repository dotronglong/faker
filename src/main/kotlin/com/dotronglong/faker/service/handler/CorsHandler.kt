package com.dotronglong.faker.service.handler

import com.dotronglong.faker.contract.Handler
import com.dotronglong.faker.pojo.MutableResponse
import com.dotronglong.faker.service.plugin.CorsResponsePlugin
import com.dotronglong.faker.service.plugin.JsonResponsePlugin
import org.springframework.http.server.reactive.ServerHttpRequest
import org.springframework.http.server.reactive.ServerHttpResponse
import reactor.core.publisher.Mono

class CorsHandler : Handler {
    override fun handle(request: ServerHttpRequest, response: ServerHttpResponse): Mono<Void> {
        return JsonResponsePlugin(response).run(MutableResponse(200, "", CorsResponsePlugin.HEADERS), true)
    }
}