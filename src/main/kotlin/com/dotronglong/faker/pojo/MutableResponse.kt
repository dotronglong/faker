package com.dotronglong.faker.pojo

data class MutableResponse(
        var statusCode: Int,
        var body: String,
        val headers: MutableMap<String, String>
)