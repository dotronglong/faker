package com.dotronglong.faker.service.watcher

import com.dotronglong.faker.contract.Watcher
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.io.IOException
import java.nio.file.FileSystems
import java.nio.file.Path
import java.nio.file.StandardWatchEventKinds.OVERFLOW
import java.nio.file.WatchEvent
import java.nio.file.WatchKey
import java.util.function.BiConsumer

@Service
class SourceWatcher : Watcher {
    private val logger: Logger = LoggerFactory.getLogger(javaClass)

    override fun watch(folder: Path, events: List<WatchEvent.Kind<*>>, consumer: BiConsumer<Path, WatchEvent.Kind<*>>) {
        Thread {
            try {
                val watcher = FileSystems.getDefault().newWatchService()
                folder.register(watcher, events.toTypedArray())
                logger.info("Start watching {} ...", folder.toString())
                while (true) {
                    var key: WatchKey
                    key = try {
                        watcher.take()
                    } catch (e: InterruptedException) {
                        logger.warn("Unable to retrieve watch key. Exception: {}", e.message)
                        break
                    }
                    for (event in key.pollEvents()) {
                        val kind = event.kind()
                        if (kind === OVERFLOW) {
                            continue
                        }

                        @Suppress("UNCHECKED_CAST")
                        consumer.accept(folder.resolve((event as WatchEvent<Path>).context()), kind)
                    }
                    if (!key.reset()) {
                        break
                    }
                }
            } catch (e: IOException) {
                logger.error("Unable to watch source. Exception: {}", e.message)
            }
        }.start()
    }
}