package com.dotronglong.faker.service.plugin

import com.dotronglong.faker.contract.Plugin
import com.dotronglong.faker.pojo.MutableResponse
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import reactor.core.publisher.Mono

class DelayResponsePlugin : Plugin {
    override val name: String
        get() = "delay"

    override fun run(response: MutableResponse, parameters: Any): Mono<Void> {
        return Mono.create { s ->
            if (parameters is Int && parameters > 0) {
                response.body = response.body.replace("me", "you")
                GlobalScope.launch {
                    delay((parameters).toLong())
                    s.success()
                }
            } else {
                throw Exception("Delay must be an integer and greater than zero")
            }
        }
    }
}