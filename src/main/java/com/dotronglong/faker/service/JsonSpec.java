package com.dotronglong.faker.service;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.Map;

@Getter
@Setter
@Builder
@AllArgsConstructor
public class JsonSpec {
    private Config config;
    private Request request;
    private Response response;

    @Getter
    @Setter
    @Builder
    @AllArgsConstructor
    public static class Config {
        private int delay; // delay in milliseconds
    }

    @Getter
    @Setter
    @Builder
    @AllArgsConstructor
    public static class Request {
        private String method;
        private String scheme;
        private String host;
        private int port;
        private String path;
        private Map<String, String> headers;
    }

    @Getter
    @Setter
    @Builder
    @AllArgsConstructor
    public static class Response {
        int code;
        private Map<String, String> headers;
        private Object body;
    }
}
