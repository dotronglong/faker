package com.dotronglong.faker.service.plugin

import com.dotronglong.faker.contract.Plugin
import com.dotronglong.faker.pojo.MutableResponse
import org.springframework.http.server.reactive.ServerHttpRequest
import reactor.core.publisher.Mono
import java.util.regex.Pattern

class RequestPlugin : BasePlugin(), Plugin {
    override val name: String
        get() = "request"

    override fun run(request: ServerHttpRequest, response: MutableResponse, arguments: Map<String, Any>?): Mono<Void> {
        return Mono.create { s ->
            try {
                val body = response.body
                val pattern = Pattern.compile("#request:(?<type>\\w+):?(?<value>[^:#]*)?#")
                val matcher = pattern.matcher(body)
                while (matcher.find()) {
                    val type = matcher.group("type")
                    val find = matcher.group()
                    when (type) {
                        "query" -> {
                            val paramName = matcher.group("value")
                            val replace = request.queryParams.getFirst(paramName)
                            response.body = response.body.replaceFirst(find, replace ?: find)
                        }

                        "url" -> {
                            val replace = request.uri.toString()
                            response.body = response.body.replaceFirst(find, replace)
                        }

                        "path" -> {
                            val replace = request.uri.path
                            response.body = response.body.replaceFirst(find, replace)
                        }

                        "headers" -> {
                            val headerName = matcher.group("value")
                            val replace = request.headers.getFirst(headerName)
                            response.body = response.body.replaceFirst(find, replace ?: find)
                        }
                    }
                }
                s.success()
            } catch (e: Exception) {
                s.error(e)
            }
        }
    }
}