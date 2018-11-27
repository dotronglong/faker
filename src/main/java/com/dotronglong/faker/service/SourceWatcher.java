package com.dotronglong.faker.service;

import com.dotronglong.faker.contract.Watcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.*;
import java.util.List;
import java.util.function.BiConsumer;

import static java.nio.file.StandardWatchEventKinds.OVERFLOW;

@Service
public class SourceWatcher implements Watcher {
    private static final Logger logger = LoggerFactory.getLogger(SourceWatcher.class);

    @Override
    public void watch(Path folder, List<WatchEvent.Kind<?>> events, BiConsumer<Path, WatchEvent.Kind<?>> consumer) {
        Thread thread = new Thread(() -> {
            try {
                WatchService watcher = FileSystems.getDefault().newWatchService();
                folder.register(watcher, (WatchEvent.Kind<?>[]) events.toArray());
                logger.info("Start watching {} ...", folder.toString());

                for (;;) {
                    WatchKey key;
                    try {
                        key = watcher.take();
                    } catch (InterruptedException e) {
                        logger.warn("Unable to retrieve watch key. Exception: {}", e.getMessage());
                        return;
                    }

                    for (WatchEvent<?> event: key.pollEvents()) {
                        WatchEvent.Kind<?> kind = event.kind();

                        if (kind == OVERFLOW) {
                            continue;
                        }

                        @SuppressWarnings("unchecked") WatchEvent<Path> ev = (WatchEvent<Path>) event;
                        consumer.accept(folder.resolve(ev.context()), kind);
                    }

                    if (!key.reset()) {
                        break;
                    }
                }
            } catch (IOException e) {
                logger.error("Unable to watch source. Exception: {}", e.getMessage());
            }
        });
        thread.start();
    }
}
