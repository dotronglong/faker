package com.dotronglong.faker

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.boot.test.web.client.getForEntity
import org.springframework.boot.test.web.client.postForEntity
import org.springframework.http.HttpMethod
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

	@Test
	fun testResponseWithCorsEnabled() {
		val entity = restTemplate.getForEntity<String>("/cors")
		assertThat(entity.statusCode).isEqualTo(HttpStatus.OK)
		assertThat(entity.body).isEqualTo("{}")
		assertThat(entity.headers.accessControlAllowCredentials).isTrue()
		assertThat(entity.headers.accessControlAllowOrigin).isEqualTo("*")
		assertThat(entity.headers.accessControlExposeHeaders).isEqualTo(listOf(
				"Authorization",
				"Access-Control-Allow-Origin",
				"Access-Control-Allow-Credentials"
		))
		assertThat(entity.headers.accessControlAllowMethods).isEqualTo(listOf(
				HttpMethod.GET,
				HttpMethod.POST,
				HttpMethod.PUT,
				HttpMethod.PATCH,
				HttpMethod.DELETE,
				HttpMethod.OPTIONS
		))
	}

	@Test
	fun testResponseWithRandomPluginEnabled() {
		val entity = restTemplate.getForEntity<Any>("/v1/users?random")
		assertThat(entity.statusCode).isEqualTo(HttpStatus.OK)
		assertThat(entity.body is List<*>).isTrue()

		val items = (entity.body as List<*>)
		for (item in items) {
			assertThat(item is Map<*, *>).isTrue()
			val people = (item as Map<*, *>)
			if ((people["id"] as Int) == 1) {
				assertThat(people["name"] is String)
				assertThat((people["name"] as String).length).isEqualTo(10)
			} else if ((people["id"] as Int) == 2) {
				assertThat(people["name"] is String)
				assertThat((people["name"] as String).isNotEmpty()).isTrue()
				assertThat(people["age"] is Int)
				assertThat(people["age"] as Int).isBetween(10, 100)
				assertThat(people["email"] is String)
				assertThat((people["email"] as String).isNotEmpty()).isTrue()
				assertThat((people["email"] as String).indexOf("@")).isGreaterThan(0)
			}
		}
	}

	@Test
	fun testResponseWithTimestampPluginEnabled() {
		val entity = restTemplate.getForEntity<Any>("/v1/users?timestamp")
		assertThat(entity.statusCode).isEqualTo(HttpStatus.OK)
		assertThat(entity.body is List<*>).isTrue()

		val items = (entity.body as List<*>)
		for (item in items) {
			assertThat(item is Map<*, *>).isTrue()
			val people = (item as Map<*, *>)
			if ((people["id"] as Int) == 2) {
				assertThat(people["created_at"] is Long) // verify created_at is less than or equal current timestamp
				assertThat(people["created_at"] as Long).isLessThanOrEqualTo(System.currentTimeMillis())
			}
		}
	}
}