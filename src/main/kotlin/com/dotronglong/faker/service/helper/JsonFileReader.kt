package com.dotronglong.faker.service.helper

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.util.stream.Stream

class JsonFileReader {
    val mapper = ObjectMapper()

    init {
        mapper.registerKotlinModule()
    }

    inline fun <reified T> read(resource: String): T? {
        val input = javaClass.classLoader.getResourceAsStream(resource)
                ?: throw IOException("Unable to read file $resource")
        val reader = BufferedReader(InputStreamReader(input))
        val contentBuilder = StringBuilder()
        val stream: Stream<String> = reader.lines()
        stream.forEach { s -> contentBuilder.append(s).append("\n") }
        val json = contentBuilder.toString()
        if (json.isEmpty()) {
            return null
        }

        return mapper.readValue(json)
    }
}