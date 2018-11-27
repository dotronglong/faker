package com.dotronglong.faker;

import org.junit.Test;
import org.springframework.http.MediaType;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.junit.Assert.*;

public class JsonRequestTest extends BaseRequestTest {
    @Test
    public void testRequestWithoutQuery() throws Exception {
        mvc.perform(get("/v1/users"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.[0].id").value(1))
                .andExpect(jsonPath("$.[1].id").value(2));
    }

    @Test
    public void testRequestQuery() throws Exception {
        mvc.perform(get("/v1/users?name=John"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("John"));
    }

    @Test
    public void testRequestNotFound() throws Exception {
        mvc.perform(get("/v1/unknown"))
                .andExpect(status().isNotFound());
    }

    @Test
    public void testRequestWithDelay() throws Exception {
        long start = System.currentTimeMillis();
        mvc.perform(post("/v1/auth"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON));
        long stop = System.currentTimeMillis();
        long duration = stop - start;
        System.out.println("Request is executed in " + duration + " ms");
        assertTrue(duration > 500);
    }
}
