package com.dotronglong.faker.controller

import com.dotronglong.faker.contract.Router
import com.dotronglong.faker.service.handler.NotFoundHandler
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.server.reactive.ServerHttpRequest
import org.springframework.http.server.reactive.ServerHttpResponse
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestMethod
import org.springframework.web.bind.annotation.RestController
import reactor.core.publisher.Mono

@RestController
class MainController @Autowired constructor(val router: Router) {

    @RequestMapping(
            path = ["/**"],
            method = [
                RequestMethod.GET,
                RequestMethod.POST,
                RequestMethod.PUT,
                RequestMethod.PATCH,
                RequestMethod.DELETE
            ]
    )
    fun handle(request: ServerHttpRequest, response: ServerHttpResponse): Mono<Void> {
        var handler = router.match(request)
        if (handler == null) {
            handler = NotFoundHandler()
        }

        return handler.handle(request, response)
    }
}