package com.dotronglong.faker.contract;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.WatchEvent;
import java.util.Arrays;
import java.util.List;
import java.util.function.BiConsumer;

import static java.nio.file.StandardWatchEventKinds.*;

public interface Watcher {
    /**
     * Watch a folder
     * @param folder a Path object
     * @param events an array of event's types
     * @param consumer a consumer to be a callback
     */
    void watch(Path folder, List<WatchEvent.Kind<?>> events, BiConsumer<Path, WatchEvent.Kind<?>> consumer);

    /**
     * Watch a folder
     * @param folder a string represents for folder
     * @param events an array of event's types
     * @param consumer a consumer to be a callback
     */
    default void watch(String folder, List<WatchEvent.Kind<?>> events, BiConsumer<Path, WatchEvent.Kind<?>> consumer) {
        watch(Paths.get(folder), events, consumer);
    }

    /**
     * Watch create, delete and modify event of folder
     * @param folder a string represents for folder
     * @param consumer a consumer to be a callback
     */
    default void watch(String folder, BiConsumer<Path, WatchEvent.Kind<?>> consumer) {
        watch(folder, Arrays.asList(
                ENTRY_CREATE,
                ENTRY_DELETE,
                ENTRY_MODIFY
        ), consumer);
    }
}
