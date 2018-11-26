package com.dotronglong.faker.contract;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@FunctionalInterface
public interface Handler {
    void handle(final HttpServletRequest request, final HttpServletResponse response);
}
