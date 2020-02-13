package com.dotronglong.faker.pojo

data class Spec(
        val plugins: List<Plugin>?,
        val request: Request,
        val response: Response
) {
    data class Request(
            val method: String,
            val scheme: String?,
            val host: String?,
            val port: Int?,
            val path: String,
            val headers: Map<String, String>?
    )

    data class Response(
            var statusCode: Int?,
            var headers: Map<String, String>?,
            var body: Any?
    )

    data class Plugin(
            val name: String,
            val args: Map<String, Any>?
    )
}