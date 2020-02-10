package com.dotronglong.faker

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.boot.test.web.client.getForEntity
import org.springframework.http.HttpStatus

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class FakerApplicationTests(@Autowired val restTemplate: TestRestTemplate) {

	@Test
	fun testDelayingResponse() {
		val start = System.currentTimeMillis()
		val entity = restTemplate.getForEntity<String>("/delay")
		val stop = System.currentTimeMillis()
		assertThat(stop - start).isGreaterThan(500)
		assertThat(entity.statusCode).isEqualTo(HttpStatus.OK)
		assertThat(entity.body).isEqualTo("{\"token\":\"some-token-me\"}")
	}

}
