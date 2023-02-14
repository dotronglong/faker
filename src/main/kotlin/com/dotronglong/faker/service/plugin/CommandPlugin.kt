package com.dotronglong.faker.service.plugin

import com.dotronglong.faker.contract.Plugin
import com.dotronglong.faker.pojo.MutableResponse
import com.dotronglong.faker.service.helper.JsonStringModifier
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.springframework.http.server.reactive.ServerHttpRequest
import reactor.core.publisher.Mono
import java.io.BufferedReader
import java.io.File
import java.io.InputStreamReader
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.TimeUnit

class CommandPlugin : Plugin {
    private companion object {
        val cache = ConcurrentHashMap<Int, CommandPluginCacheItem>()
    }

    override val name: String
        get() = "command"

    override fun run(request: ServerHttpRequest, response: MutableResponse, arguments: Map<String, Any>?): Mono<Void> {
        return Mono.create { s ->
            if (response.body.isEmpty()) {
                s.error(Exception("Body must not be empty"))
                return@create
            }
            try {
                GlobalScope.launch {
                    val commandPluginArguments = getCommandPluginArguments(arguments)
                    val content = execute(commandPluginArguments)
                    replace(response, commandPluginArguments, content)
                    s.success()
                }
            } catch (e: Exception) {
                s.error(e)
            }
        }
    }

    data class CommandPluginArguments(
            val dir: String,
            val cmd: List<String>,
            val prop: String,
            val timeout: Int,
            val cache: Boolean,
            val ttl: Int,
            val section: String // header or body
    )

    data class CommandPluginCacheItem(
            val content: String,
            val timestamp: Long
    )

    @Suppress("UNCHECKED_CAST")
    private fun getCommandPluginArguments(arguments: Map<String, Any>?): CommandPluginArguments {
        if (arguments == null) {
            throw Exception("No arguments found")
        }

        return CommandPluginArguments(
                arguments["dir"] as String? ?: System.getProperty("user.dir"),
                arguments["cmd"] as List<String>,
                arguments["prop"] as String,
                arguments["timeout"] as Int? ?: 1000,
                arguments["cache"] as Boolean? ?: false,
                arguments["ttl"] as Int? ?: 60000,
                arguments["section"] as String? ?: "body"
        )
    }

    private fun execute(arguments: CommandPluginArguments): String {
        val key = arguments.hashCode()
        if (arguments.cache) {
            val item = cache[key]
            if (item != null && item.timestamp + arguments.ttl > System.currentTimeMillis()) {
                return item.content
            }
        }
        val workDir = File(arguments.dir)
        if (!workDir.exists() || !workDir.isDirectory) {
            throw Exception("${arguments.dir} does not exist or is not a directory")
        }
        val processBuilder = ProcessBuilder().directory(workDir)
        processBuilder.command(arguments.cmd)
        val process = processBuilder.start()
        val output = StringBuilder()
        val reader = BufferedReader(InputStreamReader(process.inputStream))
        var line: String?
        do {
            line = reader.readLine()
            if (line == null) {
                break
            }
            output.append(line)
        } while (true)

        val ok = process.waitFor(arguments.timeout.toLong(), TimeUnit.MILLISECONDS)
        if (!ok) {
            throw Exception("command exits with an error")
        }

        val content = output.toString()
        if (arguments.cache) {
            cache[key] = CommandPluginCacheItem(content, System.currentTimeMillis())
        }
        return content
    }

    private fun replace(response: MutableResponse, arguments: CommandPluginArguments, content: String) {
        if (arguments.section == "header") {
            response.headers[arguments.prop] = content
        } else if (arguments.section == "body") {
            val replacer = JsonStringModifier(response.body)
            response.body = replacer.replace(arguments.prop, content)
        }
    }
}