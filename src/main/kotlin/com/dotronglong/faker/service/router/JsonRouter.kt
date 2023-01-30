package com.dotronglong.faker.service.router

import com.dotronglong.faker.config.FakerApplicationProperties
import com.dotronglong.faker.contract.Handler
import com.dotronglong.faker.contract.Router
import com.dotronglong.faker.contract.Watcher
import com.dotronglong.faker.pojo.Spec
import com.dotronglong.faker.service.handler.JsonSpecHandler
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.server.reactive.ServerHttpRequest
import org.springframework.stereotype.Service
import java.io.File
import java.io.IOException
import java.net.MalformedURLException
import java.nio.charset.Charset
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardWatchEventKinds.*
import java.nio.file.WatchEvent
import java.util.concurrent.ConcurrentHashMap
import java.util.function.BiConsumer
import java.util.regex.Pattern
import java.util.stream.Stream

@Service
class JsonRouter @Autowired constructor(
        private val properties: FakerApplicationProperties,
        private val watcher: Watcher
) : Router {
    private val logger: Logger = LoggerFactory.getLogger(javaClass)
    private val charset: Charset = StandardCharsets.UTF_8
    private val specs: ConcurrentHashMap<String, Spec> = ConcurrentHashMap()
    private val mapper: ObjectMapper = ObjectMapper()

    init {
        if (properties.source.isNotEmpty()) {
            mapper.registerKotlinModule()
            if (scan(properties.source)) {
                logger.info("Scanning is completed.")
            } else {
                logger.error("Scanning is failed.")
            }
            if (properties.watch) {
                watch(properties.source)
            }
        } else {
            logger.error("Source is not properly configured.")
        }
        logger.info("Version: {}", properties.version)
    }

    override fun match(request: ServerHttpRequest): Handler? {
        for (spec: Spec in specs.values) {
            if (!request.method!!.matches(spec.request.method.toUpperCase())) {
                continue
            }

            try {
                val url = request.uri.toURL()
                if (spec.request.scheme != null && spec.request.scheme != url.protocol) {
                    continue
                }
                if (spec.request.host != null && spec.request.host != url.host) {
                    continue
                }
                if (spec.request.port != null && spec.request.port != url.port) {
                    continue
                }
                if (spec.request.path.isEmpty()) {
                    continue
                }

                var path = url.path
                if (url.query != null) {
                    path += "?" + url.query
                }
                if (spec.request.path != path) {
                    val pattern = Pattern.compile(spec.request.path)
                    if (!pattern.matcher(path).matches()) {
                        continue
                    }
                }
                if (spec.request.headers != null) {
                    var isHeaderOK = true
                    for (key in spec.request.headers.keys) {
                        if (request.headers[key] == null) {
                            isHeaderOK = false
                            break
                        }
                    }
                    if (!isHeaderOK) {
                        continue
                    }
                }

                return JsonSpecHandler(spec)
            } catch (e: MalformedURLException) {
                logger.error("Unable to parse request's URL. Exception: {}", e.message)
            }
        }

        return null
    }

    private fun scan(source: String): Boolean {
        val folder = File(source)
        if (!folder.exists() || !folder.canRead() || !folder.isDirectory) {
            logger.error("Source folder {} does not exist or not readable or is not a directory.", folder.absolutePath)
            return false
        }

        val files = folder.listFiles()
        for (file in files!!) {
            if (file.isDirectory) {
                scan(file.path)
            } else if (!parse(file)) {
                logger.warn("Unable to parse file {}", file.name)
            }
        }
        return true
    }

    private fun parse(file: File): Boolean {
        if (!isValid(file)) {
            logger.warn("File {} is not valid", file.name)
            return false
        }

        val spec = read(file)
        if (spec == null) {
            logger.error("Unable to read file {}", file.absolutePath)
            return false
        }

        /**
         * File.absolutePath property used instead of file.name for uniqueness,
         * since filenames can be the same
         */
        this.specs.putIfAbsent(file.absolutePath, spec)
        logger.info("Parsed file {}", file.absolutePath)
        return true
    }

    private fun isValid(file: File): Boolean {
        return !file.isDirectory
                && file.exists()
                && file.canRead()
                && getFileExtension(file) == "json"
                && !this.specs.containsKey(file.name)
    }

    private fun getFileExtension(file: File): String {
        val parts = file.name.split(".")
        return parts[parts.size - 1]
    }

    private fun read(file: File): Spec? {
        try {
            val contentBuilder = StringBuilder()
            val stream: Stream<String> = Files.lines(file.toPath(), charset)
            stream.forEach { s -> contentBuilder.append(s).append("\n") }
            val json: String = contentBuilder.toString()
            if (json.isNotEmpty()) {
                return mapper.readValue<Spec>(json)
            }
        } catch (e: IOException) {
            logger.error("Unable to read file. Exception: {}", e.message)
        }

        return null
    }

    private fun watch(source: String) {
        watcher.watch(source, BiConsumer { file: Path, kind: WatchEvent.Kind<*> ->
            val fileName = file.fileName.toString()
            when {
                kind === ENTRY_MODIFY -> {
                    logger.info("Changed on file {}", fileName)
                    specs.remove(fileName)
                    parse(file.toFile())
                }
                kind === ENTRY_DELETE -> {
                    specs.remove(fileName)
                    logger.info("Removed file {}", fileName)
                }
                kind === ENTRY_CREATE -> {
                    parse(file.toFile())
                }
            }
        })
    }
}