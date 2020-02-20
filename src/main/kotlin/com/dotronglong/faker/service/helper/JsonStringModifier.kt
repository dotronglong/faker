package com.dotronglong.faker.service.helper

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import java.util.*

class JsonStringModifier constructor(private val json: String) {
    private val mapper = ObjectMapper()

    init {
        mapper.registerKotlinModule()
    }

    fun replace(key: String, value: Any): String {
        val body = mapper.readValue<LinkedHashMap<String, Any>>(json)
        val props = key.split(".")
        var map = body
        @Suppress("UNCHECKED_CAST")
        for (i in props.indices) {
            val prop = props[i]
            if (!map.containsKey(prop)) {
                throw Exception("$prop does not exist in body")
            }
            if (i == props.size - 1) {
                map[prop] = value
                break
            }
            if (map[prop] !is LinkedHashMap<*, *>) {
                throw Exception("$prop must be an object")
            }
            map = map[prop] as LinkedHashMap<String, Any>
        }

        return mapper.writeValueAsString(body)
    }
}