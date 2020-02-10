package com.dotronglong.faker.contract

import org.springframework.http.server.reactive.ServerHttpRequest

interface Router {
    fun match(request: ServerHttpRequest): Handler?
}