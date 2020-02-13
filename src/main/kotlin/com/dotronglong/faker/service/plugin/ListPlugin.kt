package com.dotronglong.faker.service.plugin

import com.dotronglong.faker.contract.Plugin
import com.dotronglong.faker.pojo.MutableResponse
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import reactor.core.publisher.Mono
import java.util.LinkedHashMap

class ListPlugin : Plugin {
    private val mapper = ObjectMapper()

    init {
        mapper.registerKotlinModule()
    }

    data class ListConfiguration(
            val count: Int,
            val field: String?,
            val item: Any
    )

    override val name: String
        get() = "list"

    override fun run(response: MutableResponse, parameters: Any): Mono<Void> {
        return Mono.create { s ->
            if (response.body.isEmpty()) {
                s.error(Exception("Body must not be empty"))
                return@create
            }
            try {
                val config = getConfiguration(parameters)
                if (config.count <= 0) {
                    s.error(Exception("count must be greater than zero"))
                    return@create
                }
                val list: MutableList<Any> = ArrayList()
                for (i in 1..config.count) {
                    list.add(config.item)
                }
                if (config.field == null) { /* Whole body will be used */
                    response.body = mapper.writeValueAsString(list)
                } else { /* Only one field will be replaced */
                    val body = mapper.readValue<LinkedHashMap<String, Any>>(response.body)
                    if (!body.containsKey(config.field)) {
                        s.error(Exception("${config.field} does not exist in body"))
                        return@create
                    }
                    body[config.field] = list
                    response.body = mapper.writeValueAsString(body)
                }
                s.success()
            } catch (e: Exception) {
                s.error(e)
            }
        }
    }

    private fun getConfiguration(parameters: Any) : ListConfiguration {
        if (parameters !is Map<*, *>) {
            throw Exception("Invalid Argument")
        }
        val config = (parameters as Map<*, *>)
        return ListConfiguration(
                config["count"] as Int,
                config["field"] as String?,
                config["item"] as Any
        )
    }
}