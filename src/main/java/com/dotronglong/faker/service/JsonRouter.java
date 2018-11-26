package com.dotronglong.faker.service;

import com.dotronglong.faker.config.FakerApplicationProperties;
import com.dotronglong.faker.contract.Handler;
import com.dotronglong.faker.contract.Router;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;

@Service
public class JsonRouter implements Router {
    private static final Logger logger = LoggerFactory.getLogger(JsonRouter.class);
    private FakerApplicationProperties properties;

    @Autowired
    public JsonRouter(FakerApplicationProperties properties) {
        this.properties = properties;
    }

    @Override
    public Handler match(HttpServletRequest request) {
        return null;
    }

    @PostConstruct
    public void init() {
        String source = properties.getSource();
        if (source.isEmpty()) {
            logger.error("Source is not properly configured.");
        }
    }
}
