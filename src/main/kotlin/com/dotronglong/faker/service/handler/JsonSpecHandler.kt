package com.dotronglong.faker.service.handler

import com.dotronglong.faker.contract.Handler
import com.dotronglong.faker.contract.Plugin
import com.dotronglong.faker.pojo.MutableResponse
import com.dotronglong.faker.pojo.Spec
import com.dotronglong.faker.service.plugin.DelayResponsePlugin
import com.dotronglong.faker.service.plugin.JsonResponsePlugin
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.server.reactive.ServerHttpRequest
import org.springframework.http.server.reactive.ServerHttpResponse
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

class JsonSpecHandler constructor(private val spec: Spec) : Handler {
    private val logger: Logger = LoggerFactory.getLogger(javaClass)
    private val plugins: HashMap<String, Plugin> = HashMap()
    private val mapper = ObjectMapper()

    init {
        mapper.registerKotlinModule()
        listOf<Plugin>(
                DelayResponsePlugin()
        ).forEach { plugin -> plugins[plugin.name] = plugin }
    }

    override fun handle(request: ServerHttpRequest, response: ServerHttpResponse): Mono<Void> {
        return Mono.create { s ->
            val mutableResponse = MutableResponse(HttpStatus.OK.value(), mapper.writeValueAsString(spec.response.body), HashMap())
            val tasks: MutableList<Mono<Void>> = ArrayList()
            if (spec.plugins != null) {
                for ((name, parameters) in spec.plugins) {
                    if (plugins.containsKey(name)) {
                        if (plugins[name] != null) {
                            tasks.add(plugins[name]!!.run(mutableResponse, parameters))
                        }
                    }
                }
            }
            var completes = tasks.size
            val done = {
                if (--completes == 0) {
                    JsonResponsePlugin(response).run(mutableResponse, true)
                            .subscribe({}, { e -> logger.error(e.message) }, { s.success() })
                }
            }
            Flux.fromIterable(tasks).subscribe { task -> task.subscribe({}, { e -> logger.error(e.message); done() }, done) }
        }
    }
}