package com.dotronglong.faker.service.plugin

import com.dotronglong.faker.contract.Plugin
import com.dotronglong.faker.pojo.MutableResponse
import reactor.core.publisher.Mono

class TimestampPlugin : BasePlugin(), Plugin {
    override val name: String
        get() = "timestamp"

    override fun run(response: MutableResponse, arguments: Map<String, Any>?): Mono<Void> {
        return Mono.create { s ->
            try {
                val find = "#timestamp#"
                val replace = timestamp(arguments ?: mapOf())
                response.body = response.body.replace("\"$find\"", "$replace")
                s.success()
            } catch (e: Exception) {
                s.error(e)
            }
        }
    }

    private enum class TimestampMeter {
        SECONDS, MILLISECONDS, NANOSECONDS
    }

    private fun timestamp(arguments: Map<String, Any>): Long {
        var meter = TimestampMeter.MILLISECONDS
        if (arguments.containsKey("in")) {
            when (arguments["in"]) {
                "milliseconds" -> meter = TimestampMeter.MILLISECONDS
                "nanoseconds" -> meter = TimestampMeter.NANOSECONDS
                "seconds" -> meter = TimestampMeter.SECONDS
            }
        }

        val timestamp = System.currentTimeMillis()
        if (meter == TimestampMeter.NANOSECONDS) {
            return timestamp * 1000
        } else if (meter == TimestampMeter.SECONDS) {
            return timestamp / 1000
        }

        return timestamp
    }
}