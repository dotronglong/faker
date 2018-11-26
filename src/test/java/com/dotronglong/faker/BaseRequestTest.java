package com.dotronglong.faker;

import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        classes = FakerApplication.class
)
@TestPropertySource(
        locations = "classpath:test.properties"
)
@RunWith(SpringRunner.class)
@AutoConfigureMockMvc
public abstract class BaseRequestTest {
    @Autowired protected MockMvc mvc;
}
