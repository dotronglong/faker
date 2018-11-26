package com.dotronglong.faker.contract;

import javax.servlet.http.HttpServletRequest;

public interface Router {
    Handler match(final HttpServletRequest request);
}
