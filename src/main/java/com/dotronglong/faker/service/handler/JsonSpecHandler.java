package com.dotronglong.faker.service.handler;

import com.alibaba.fastjson.JSON;
import com.dotronglong.faker.contract.Handler;
import com.dotronglong.faker.service.JsonSpec;
import lombok.Getter;
import lombok.Setter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Objects;

@Getter
@Setter
public class JsonSpecHandler implements Handler {
    private static final Logger logger = LoggerFactory.getLogger(JsonSpecHandler.class);
    private static final String CONTENT_JSON_UTF8 = "application/json; charset=utf-8";
    private JsonSpec spec;

    public JsonSpecHandler(JsonSpec spec) {
        this.spec = spec;
    }

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response) {
        JsonSpec.Config config = spec.getConfig();
        if (!Objects.isNull(config)) {
            if (config.getDelay() > 0) {
                try {
                    Thread.sleep(config.getDelay());
                } catch (InterruptedException e) {
                    logger.warn("Unable to sleep in {} ms. Exception: {}", config.getDelay(), e.getMessage());
                }
            }
        }

        response.setContentType(CONTENT_JSON_UTF8);
        JsonSpec.Response specResponse = spec.getResponse();
        if (!Objects.isNull(specResponse.getHeaders())) {
            specResponse.getHeaders().forEach(response::addHeader);
        }

        Object body = specResponse.getBody();
        if (!Objects.isNull(body)) {
            try {
                response.getWriter().write(JSON.toJSONString(specResponse.getBody()));
            } catch (IOException e) {
                logger.error("Unable to write response's body. Exception: {}", e.getMessage());
            }
        }
    }
}
