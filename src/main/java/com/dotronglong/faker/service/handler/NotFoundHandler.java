package com.dotronglong.faker.service.handler;

import com.dotronglong.faker.contract.Handler;
import org.springframework.http.HttpStatus;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class NotFoundHandler implements Handler {
    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response) {
        response.setStatus(HttpStatus.NOT_FOUND.value());
    }
}
