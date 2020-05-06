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

    @Test
    fun testResponseWithRegExp() {
        val entity = restTemplate.getForEntity<String>("/v1/users/6789")
        assertThat(entity.statusCode).isEqualTo(HttpStatus.OK)
        assertThat(entity.body).isEqualTo("{\"id\":1,\"name\":\"John\"}")
    }

    @Test
    fun testResponseWithRandomPluginEnabled() {
        val entity = restTemplate.getForEntity<Any>("/random")
        assertThat(entity.statusCode).isEqualTo(HttpStatus.OK)
        assertThat(entity.body is List<*>).isTrue()

        val items = (entity.body as List<*>)
        for (item in items) {
            assertThat(item is Map<*, *>).isTrue()
            val people = (item as Map<*, *>)
            if ((people["id"] as Int) == 1) {
                assertThat(people["name"] is String)
                assertThat((people["name"] as String).length).isGreaterThan(0)
                assertThat((people["name"] as String).split(" ").size).isEqualTo(5)
                assertThat(people["is_enabled"] is Boolean)
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
        val entity = restTemplate.getForEntity<Any>("/timestamp")
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

    @Test
    fun testResponseWithListPluginEnabledWithoutProp() {
        val entity = restTemplate.getForEntity<Any>("/list?body")
        assertThat(entity.statusCode).isEqualTo(HttpStatus.OK)
        assertThat(entity.body is List<*>).isTrue()

        val items = (entity.body as List<*>)
        assertThat(items.size).isEqualTo(5)
        for (item in items) {
            assertThat(item is Map<*, *>).isTrue()
            val people = (item as Map<*, *>)
            assertThat(people["name"]).isEqualTo("John")
            assertThat(people["age"]).isEqualTo(35)
        }
    }

    @Test
    fun testResponseWithListPluginEnabledWithProp() {
        val entity = restTemplate.getForEntity<Any>("/list?prop")
        assertThat(entity.statusCode).isEqualTo(HttpStatus.OK)
        assertThat(entity.body is Map<*, *>).isTrue()

        val body = (entity.body as Map<*, *>)
        assertThat(body.containsKey("data")).isTrue()
        assertThat(body["data"] is List<*>).isTrue()
        val items = body["data"] as List<*>
        assertThat(items.size).isEqualTo(5)
        for (item in items) {
            assertThat(item is Map<*, *>).isTrue()
            val people = (item as Map<*, *>)
            assertThat(people["name"]).isEqualTo("John")
            assertThat(people["age"]).isEqualTo(35)
        }
    }

    @Test
    fun testResponseWithListPluginEnabledWithPropInProp() {
        val entity = restTemplate.getForEntity<Any>("/list?prop_in_prop")
        assertThat(entity.statusCode).isEqualTo(HttpStatus.OK)
        assertThat(entity.body is Map<*, *>).isTrue()

        val body = (entity.body as Map<*, *>)
        assertThat(body.containsKey("data")).isTrue()
        assertThat(body["data"] is Map<*, *>).isTrue()
        assertThat((body["data"] as Map<*, *>)["items"] is List<*>).isTrue()
        val items = (body["data"] as Map<*, *>)["items"] as List<*>
        assertThat(items.size).isEqualTo(5)
        for (item in items) {
            assertThat(item is Map<*, *>).isTrue()
            val people = (item as Map<*, *>)
            assertThat(people["name"]).isEqualTo("John")
            assertThat(people["age"]).isEqualTo(35)
        }
    }

    @Test
    fun testResponseWithCommandPluginEnabledWithPropInHeader() {
        val start = System.currentTimeMillis()
        var entity = restTemplate.getForEntity<Any>("/command?header")
        assertThat(entity.statusCode).isEqualTo(HttpStatus.OK)
        assertThat(entity.headers["api-key"]).isEqualTo(listOf("WsRDyjTKX35OfIRh95fj"))
        val lap1 = System.currentTimeMillis()

        entity = restTemplate.getForEntity<Any>("/command?header")
        assertThat(entity.statusCode).isEqualTo(HttpStatus.OK)
        assertThat(entity.headers["api-key"]).isEqualTo(listOf("WsRDyjTKX35OfIRh95fj"))
        val lap2 = System.currentTimeMillis()

        // is cache working? speed x2 at least
        assertThat(lap1 - start).isGreaterThanOrEqualTo((lap2 - lap1) * 2)
    }

    @Test
    fun testResponseWithCommandPluginEnabledWithPropInBody() {
        val start = System.currentTimeMillis()
        val entity = restTemplate.getForEntity<Any>("/command?body")
        assertThat(entity.statusCode).isEqualTo(HttpStatus.OK)
        assertThat(entity.body is Map<*, *>).isTrue()
        val body = (entity.body as Map<*, *>)
        assertThat(body["signature"] is Map<*, *>).isTrue()
        assertThat((body["signature"] as Map<*, *>)["api-key"]).isEqualTo("WsRDyjTKX35OfIRh95fj")
        val lap1 = System.currentTimeMillis()

        restTemplate.getForEntity<Any>("/command?body")
        val lap2 = System.currentTimeMillis()

        // is cache working? speed x2 at least
        assertThat(lap1 - start).isGreaterThanOrEqualTo((lap2 - lap1) * 2)
    }
}