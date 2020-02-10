package com.dotronglong.faker.contract

import java.nio.file.Path
import java.nio.file.Paths
import java.nio.file.StandardWatchEventKinds.*
import java.nio.file.WatchEvent
import java.util.function.BiConsumer


interface Watcher {
    /**
     * Watch a folder
     * @param folder a Path object
     * @param events an array of event's types
     * @param consumer a consumer to be a callback
     */
    fun watch(folder: Path, events: List<WatchEvent.Kind<*>>, consumer: BiConsumer<Path, WatchEvent.Kind<*>>)

    /**
     * Watch a folder
     * @param folder a string represents for folder
     * @param events an array of event's types
     * @param consumer a consumer to be a callback
     */
    fun watch(folder: String, events: List<WatchEvent.Kind<*>>, consumer: BiConsumer<Path, WatchEvent.Kind<*>>) = watch(Paths.get(folder), events, consumer)

    /**
     * Watch create, delete and modify event of folder
     * @param folder a string represents for folder
     * @param consumer a consumer to be a callback
     */
    fun watch(folder: String, consumer: BiConsumer<Path, WatchEvent.Kind<*>>) = watch(folder, listOf(
            ENTRY_CREATE,
            ENTRY_DELETE,
            ENTRY_MODIFY
    ), consumer)
}