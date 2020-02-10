package com.dotronglong.faker

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.boot.test.web.client.getForEntity
import org.springframework.boot.test.web.client.postForEntity
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

	@Test
	fun testNotFoundRequest() {
		val entity = restTemplate.getForEntity<String>("/not-found")
		assertThat(entity.statusCode).isEqualTo(HttpStatus.NOT_FOUND)
	}

	@Test
	fun testInvalidFormatJson() {
		val entity = restTemplate.getForEntity<String>("/some-urls")
		assertThat(entity.statusCode).isEqualTo(HttpStatus.NOT_FOUND)
	}

	@Test
	fun testRequestWithoutQuery() {
		val entity = restTemplate.getForEntity<String>("/v1/users")
		assertThat(entity.statusCode).isEqualTo(HttpStatus.OK)
		assertThat(entity.body).isEqualTo("[{\"id\":1,\"name\":\"John\"},{\"id\":2,\"name\":\"Marry\"}]")
	}

	@Test
	fun testRequestWithQuery() {
		val entity = restTemplate.getForEntity<String>("/v1/users?name=John")
		assertThat(entity.statusCode).isEqualTo(HttpStatus.OK)
		assertThat(entity.body).isEqualTo("{\"id\":1,\"name\":\"John\"}")
	}

	@Test
	fun testResponseWithStatusCode() {
		val entity = restTemplate.postForEntity<String>("/v1/users", null, String)
		assertThat(entity.statusCode).isEqualTo(HttpStatus.CREATED)
		assertThat(entity.body).isEqualTo("{\"id\":1,\"name\":\"John\"}")
	}
}
