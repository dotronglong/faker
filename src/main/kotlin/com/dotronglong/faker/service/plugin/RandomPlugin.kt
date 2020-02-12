package com.dotronglong.faker.service.plugin

import com.dotronglong.faker.contract.Plugin
import com.dotronglong.faker.pojo.MutableResponse
import com.dotronglong.faker.pojo.Names
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.util.regex.Pattern
import java.util.stream.Stream
import kotlin.random.Random

class RandomPlugin : Plugin {
    private val mapper = ObjectMapper()

    private companion object {
        var names: Names? = null
    }

    init {
        mapper.registerKotlinModule()
    }

    override val name: String
        get() = "random"

    override fun run(response: MutableResponse, parameters: Any): Mono<Void> {
        return Mono.create { s ->
            try {
                val body = response.body
                val pattern = Pattern.compile("#random:(\\w+):?([^:#]*)?#")
                val matcher = pattern.matcher(body)
                while (matcher.find()) {
                    val type = matcher.group(1)
                    val arguments = parseArguments(matcher.group(2))
                    val find = matcher.group()
                    when (type) {
                        "string" -> {
                            val replace = randomString(arguments)
                            response.body = response.body.replaceFirst(find, replace)
                        }

                        "int" -> {
                            val replace = randomInt(arguments)
                            response.body = response.body.replaceFirst("\"$find\"", replace)
                        }

                        "email" -> {
                            val replace = randomEmail()
                            response.body = response.body.replaceFirst(find, replace)
                        }

                        "name" -> {
                            val replace = randomName(arguments)
                            response.body = response.body.replaceFirst(find, replace)
                        }
                    }
                }
                s.success()
            } catch (e: Exception) {
                s.error(e)
            }
        }
    }

    private fun parseArguments(text: String): Map<String, Any> {
        val arguments = HashMap<String, Any>()
        if (text.isNotEmpty()) {
            val pairs = text.split("&")
            for (pair in pairs) {
                val args = pair.split("=")
                if (args.size == 2) {
                    arguments[args[0]] = args[1]
                }
            }
        }

        return arguments
    }

    private fun randomString(arguments: Map<String, Any>): String {
        val lower = "abcdefghijklmnopqrstuvwzyx"
        val upper = "ABCDEFGHIJKLMNOPQRSTUVWXYZ"
        val numbers = "0123456789"
        val symbols = "~!@#$%^&*()_-+="
        val space = " "

        var length = 10
        var useLowerLetters = true
        var useUpperLetters = true
        var useNumbers = true
        var useSymbols = true
        var useSpace = true

        if (arguments.containsKey("length") && (arguments["length"] as String).toInt() > 0) {
            length = (arguments["length"] as String).toInt()
        }
        if (arguments.containsKey("lowercase") && arguments["lowercase"] == "false") {
            useLowerLetters = false
        }
        if (arguments.containsKey("uppercase") && arguments["uppercase"] == "false") {
            useUpperLetters = false
        }
        if (arguments.containsKey("number") && arguments["number"] == "false") {
            useNumbers = false
        }
        if (arguments.containsKey("symbol") && arguments["symbol"] == "false") {
            useSymbols = false
        }
        if (arguments.containsKey("space") && arguments["space"] == "false") {
            useSpace = false
        }

        var characters = ""
        if (useLowerLetters) {
            characters += lower
        }
        if (useUpperLetters) {
            characters += upper
        }
        if (useNumbers) {
            characters += numbers
        }
        if (useSymbols) {
            characters += symbols
        }
        if (useSpace) {
            characters += space
        }

        var text = ""
        Flux.range(1, length).subscribe {
            val position = randomIntNumber(0, characters.length - 1)
            text += characters[position]
        }
        return text
    }

    private fun randomIntNumber(min: Int, max: Int): Int = Random.nextInt(min, max)

    private fun randomInt(arguments: Map<String, Any>): String {
        var min = 1
        var max = 100

        if (arguments.containsKey("min") && (arguments["min"] as String).toInt() > 0) {
            min = (arguments["min"] as String).toInt()
        }
        if (arguments.containsKey("max") && (arguments["max"] as String).toInt() > 0) {
            max = (arguments["max"] as String).toInt()
        }

        return "${randomIntNumber(min, max)}"
    }

    private fun randomEmail(): String {
        val extensions = listOf("com", "org", "net", "info", "biz", "dev")
        val extension = extensions[randomIntNumber(0, extensions.size - 1)]
        val domain = randomString(mapOf(
                "length" to "10",
                "symbol" to "false",
                "uppercase" to "false",
                "space" to "false"
        ))
        val account = randomString(mapOf(
                "length" to "10",
                "symbol" to "false",
                "uppercase" to "false",
                "space" to "false"
        ))
        return "$account@$domain.$extension"
    }

    @Suppress("RECEIVER_NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")
    private fun randomName(arguments: Map<String, Any>): String {
        if (names == null) {
            val resource = "names.json"
            val input = javaClass.classLoader.getResourceAsStream(resource)
                    ?: throw IOException("Unable to read file $resource")
            val reader = BufferedReader(InputStreamReader(input))
            val contentBuilder = StringBuilder()
            val stream: Stream<String> = reader.lines()
            stream.forEach { s -> contentBuilder.append(s).append("\n") }
            val json = contentBuilder.toString()
            if (json.isNotEmpty()) {
                names = mapper.readValue<Names>(json)
            }
        }

        var male = true
        if (arguments.containsKey("male") && arguments["male"] == "false"
                || arguments.containsKey("female") && arguments["female"] == "true") {
            male = false
        }

        val firstName = if (male) {
            names?.male?.get(randomIntNumber(0, (names?.male?.size ?: 1) - 1)).toString()
        } else {
            names?.female?.get(randomIntNumber(0, (names?.female?.size ?: 1) - 1)).toString()
        }
        val lastName = names?.surname?.get(randomIntNumber(0, (names?.surname?.size ?: 1) - 1))

        return "$firstName $lastName"
    }
}