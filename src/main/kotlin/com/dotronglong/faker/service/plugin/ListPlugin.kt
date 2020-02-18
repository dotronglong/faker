package com.dotronglong.faker.service.plugin

import com.dotronglong.faker.contract.Plugin
import com.dotronglong.faker.pojo.MutableResponse
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import reactor.core.publisher.Mono
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.set

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

    override fun run(response: MutableResponse, arguments: Map<String, Any>?): Mono<Void> {
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
                    val body = mapper.readValue<LinkedHashMap<String, Any>>(response.body)
                    val props = config.prop.split(".")
                    var map = body
                    @Suppress("UNCHECKED_CAST")
                    for (i in props.indices) {
                        val prop = props[i]
                        if (!map.containsKey(prop)) {
                            s.error(Exception("$prop does not exist in body"))
                            return@create
                        }
                        if (i == props.size - 1) {
                            map[prop] = list
                            break
                        }
                        if (map[prop] !is LinkedHashMap<*, *>) {
                            s.error(Exception("$prop must be an object"))
                            return@create
                        }
                        map = map[prop] as LinkedHashMap<String, Any>
                    }

                    response.body = mapper.writeValueAsString(body)
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