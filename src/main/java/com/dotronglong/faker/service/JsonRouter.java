package com.dotronglong.faker.service;

import com.alibaba.fastjson.JSON;
import com.dotronglong.faker.config.FakerApplicationProperties;
import com.dotronglong.faker.contract.Handler;
import com.dotronglong.faker.contract.Router;
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
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.regex.Pattern;
import java.util.stream.Stream;

@Service
public class JsonRouter implements Router {
    private static final Logger logger = LoggerFactory.getLogger(JsonRouter.class);
    private static final Charset charset = StandardCharsets.UTF_8;
    private FakerApplicationProperties properties;
    private List<JsonSpec> specs;

    @Autowired
    public JsonRouter(FakerApplicationProperties properties) {
        this.properties = properties;
    }

    @Override
    public Handler match(HttpServletRequest request) {
        if (Objects.isNull(specs)) {
            return null;
        }

        for (JsonSpec spec: specs) {
            JsonSpec.Request.Uri uri = spec.getRequest().getUri();
            if (!uri.getMethod().equals(request.getMethod())) {
                continue;
            }

            try {
                URL url = new URL(request.getRequestURL().toString());

                String scheme = uri.getScheme();
                if (!Objects.isNull(scheme) && !url.getProtocol().equals(scheme)) {
                    continue;
                }

                String host = uri.getHost();
                if (!Objects.isNull(host) && !url.getHost().equals(host)) {
                    continue;
                }

                int port = uri.getPort();
                if (port > 0 && port != url.getPort()) {
                    continue;
                }

                String path = uri.getPath();
                if (path.isEmpty()) {
                    continue;
                }

                if (!path.equals(url.getPath())) {
                    Pattern pattern = Pattern.compile(path);
                    if (!pattern.matcher(url.getPath()).matches()) {
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
        }
    }

    private boolean scan(String source) {
        File folder = new File(source);
        if (!folder.exists() || !folder.canRead()) {
            logger.error("Source folder {} does not exist or not readable.", folder.getAbsolutePath());
            return false;
        }

        specs = new ArrayList<>();
        for (File file: Objects.requireNonNull(folder.listFiles())) {
            if (!isValid(file)) {
                continue;
            }

            JsonSpec spec = read(file);
            if (Objects.isNull(spec)) {
                logger.error("Unable to read file {}", file.getAbsolutePath());
                return false;
            }
            specs.add(spec);
            logger.info("Parsed file {}", file.getName());
        }
        return true;
    }

    private boolean isValid(File file) {
        if (file.isDirectory()
                || !file.exists()
                || !file.canRead()) {
            return false;
        }

        if (!FilenameUtils.getExtension(file.getName()).equals("json")) {
            return false;
        }

        return true;
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
