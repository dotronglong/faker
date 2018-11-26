package com.dotronglong.faker.controller;

import com.dotronglong.faker.contract.Handler;
import com.dotronglong.faker.contract.Router;
import com.dotronglong.faker.service.handler.NotFoundHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Objects;

@RestController
public class MainController {
    private Router router;

    @Autowired
    public MainController(Router router) {
        this.router = router;
    }

    @RequestMapping(value = "/**/{[path:[^\\.]*}")
    public void index(final HttpServletRequest request, final HttpServletResponse response) {
        Handler handler = router.match(request);
        if (Objects.isNull(handler)) {
            handler = new NotFoundHandler();
        }

        handler.handle(request, response);
    }
}
