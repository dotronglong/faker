package com.dotronglong.faker.service.plugin

import com.dotronglong.faker.contract.Plugin
import com.dotronglong.faker.pojo.MutableResponse
import com.dotronglong.faker.service.helper.JsonStringModifier
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import org.springframework.http.server.reactive.ServerHttpRequest
import reactor.core.publisher.Mono

class ListPlugin : Plugin {
    private val mapper = ObjectMapper()

    init {
        mapper.registerKotlinModule()
    }

    data class ListConfiguration(
            val count: Int,
            val prop: String?,
            val item: Any
    )

    override val name: String
        get() = "list"

    override fun run(request: ServerHttpRequest, response: MutableResponse, arguments: Map<String, Any>?): Mono<Void> {
        return Mono.create { s ->
            if (response.body.isEmpty()) {
                s.error(Exception("Body must not be empty"))
                return@create
            }
            try {
                val config = getConfiguration(arguments ?: Any())
                if (config.count <= 0) {
                    s.error(Exception("count must be greater than zero"))
                    return@create
                }
                val list: MutableList<Any> = ArrayList()
                for (i in 1..config.count) {
                    list.add(config.item)
                }
                if (config.prop == null) { /* Whole body will be used */
                    response.body = mapper.writeValueAsString(list)
                } else { /* Only one field will be replaced */
                    val replacer = JsonStringModifier(response.body)
                    response.body = replacer.replace(config.prop, list)
                }
                s.success()
            } catch (e: Exception) {
                s.error(e)
            }
        }
    }

    private fun getConfiguration(arguments: Any): ListConfiguration {
        if (arguments !is Map<*, *>) {
            throw Exception("Invalid Argument")
        }
        return ListConfiguration(
                arguments["count"] as Int,
                arguments["prop"] as String?,
                arguments["item"] as Any
        )
    }
}