package com.dotronglong.faker

import com.dotronglong.faker.controller.MainController
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Import

@Import(value = [MainController::class])
@SpringBootApplication
class FakerApplication

fun main(args: Array<String>) {
    runApplication<FakerApplication>(*args)
}
