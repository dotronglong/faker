package com.dotronglong.faker.service;

import com.alibaba.fastjson.JSON;
import com.dotronglong.faker.config.FakerApplicationProperties;
import com.dotronglong.faker.contract.Handler;
import com.dotronglong.faker.contract.Router;
import com.dotronglong.faker.contract.Watcher;
import com.dotronglong.faker.service.handler.JsonSpecHandler;
import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import static java.nio.file.StandardWatchEventKinds.*;

@Service
public class JsonRouter implements Router {
    private static final Logger logger = LoggerFactory.getLogger(JsonRouter.class);
    private static final Charset charset = StandardCharsets.UTF_8;
    private FakerApplicationProperties properties;
    private Watcher watcher;
    private ConcurrentMap<String, JsonSpec> specs;

    @Autowired
    public JsonRouter(FakerApplicationProperties properties, Watcher watcher) {
        this.properties = properties;
        this.watcher = watcher;
    }

    @Override
    public Handler match(HttpServletRequest request) {
        if (Objects.isNull(specs)) {
            return null;
        }

        for (JsonSpec spec: specs.values()) {
            if (!spec.getRequest().getMethod().toUpperCase().equals(request.getMethod())) {
                continue;
            }

            try {
                String requestUrlString = request.getRequestURL().toString();
                if (!Objects.isNull(request.getQueryString())) {
                    requestUrlString += "?" + request.getQueryString();
                }
                URL url = new URL(requestUrlString);

                String scheme = spec.getRequest().getScheme();
                if (!Objects.isNull(scheme) && !url.getProtocol().equals(scheme)) {
                    continue;
                }

                String host = spec.getRequest().getHost();
                if (!Objects.isNull(host) && !url.getHost().equals(host)) {
                    continue;
                }

                int port = spec.getRequest().getPort();
                if (port > 0 && port != url.getPort()) {
                    continue;
                }

                String path = spec.getRequest().getPath();
                if (path.isEmpty()) {
                    continue;
                }

                String targetPath = url.getPath();
                if (!Objects.isNull(url.getQuery())) {
                    targetPath += "?" + url.getQuery();
                }
                if (!path.equals(targetPath)) {
                    Pattern pattern = Pattern.compile(path);
                    if (!pattern.matcher(targetPath).matches()) {
                        continue;
                    }
                }

                if (!Objects.isNull(spec.getRequest().getHeaders())) {
                    boolean headersCheck = true;
                    for (String key: spec.getRequest().getHeaders().keySet()) {
                        if (Objects.isNull(request.getHeader(key))) {
                            headersCheck = false;
                            break;
                        }
                    }
                    if (!headersCheck) {
                        continue;
                    }
                }

                return new JsonSpecHandler(spec);
            } catch (MalformedURLException e) {
                logger.error("Unable to parse request's URL. Exception: {}", e.getMessage());
            }
        }

        return null;
    }

    @PostConstruct
    public void init() {
        String source = properties.getSource();
        if (source.isEmpty()) {
            logger.error("Source is not properly configured.");
            return;
        }

        if (!scan(source)) {
            logger.error("Scanning is failed.");
            return;
        }

        if (properties.isWatch()) {
            watch(source);
        }
    }

    private void watch(String source) {
        watcher.watch(source, (file, kind) -> {
            String fileName = file.getFileName().toString();
            if (kind == ENTRY_MODIFY) {
                logger.info("Changed on file {}", fileName);
                specs.remove(fileName);
                parse(file.toFile());
            } else if (kind == ENTRY_DELETE) {
                specs.remove(fileName);
                logger.info("Removed file {}", fileName);
            } else if (kind == ENTRY_CREATE) {
                parse(file.toFile());
            }
        });
    }

    private boolean scan(String source) {
        File folder = new File(source);
        if (!folder.exists() || !folder.canRead()) {
            logger.error("Source folder {} does not exist or not readable.", folder.getAbsolutePath());
            return false;
        }

        specs = new ConcurrentHashMap<>();
        for (File file: Objects.requireNonNull(folder.listFiles())) {
            if (!parse(file)) {
                logger.warn("Unable to parse file {}", file.getName());
            }
        }
        return true;
    }

    private boolean parse(File file) {
        if (!isValid(file)) {
            logger.warn("File {} is not valid", file.getName());
            return false;
        }

        JsonSpec spec = read(file);
        if (Objects.isNull(spec)) {
            logger.error("Unable to read file {}", file.getAbsolutePath());
            return false;
        }
        specs.putIfAbsent(file.getName(), spec);
        logger.info("Parsed file {}", file.getName());
        return true;
    }

    private boolean isValid(File file) {
        return !file.isDirectory()
                && file.exists()
                && file.canRead()
                && FilenameUtils.getExtension(file.getName()).equals("json")
                && !specs.containsKey(file.getName());
    }

    private JsonSpec read(File file) {
        StringBuilder contentBuilder = new StringBuilder();
        try (Stream<String> stream = Files.lines(file.toPath(), charset)) {
            stream.forEach(s -> contentBuilder.append(s).append("\n"));
            String jsonString = contentBuilder.toString();
            if (jsonString.isEmpty()) {
                return null;
            }

            return JSON.parseObject(jsonString, JsonSpec.class);
        } catch (IOException e) {
            logger.error("Unable to read file. Exception: {}", e.getMessage());
            return null;
        }
    }
}
