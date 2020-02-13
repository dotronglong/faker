package com.dotronglong.faker.service.plugin

abstract class BasePlugin {
    /**
     * Parse inline arguments.
     * Inline Arguments starts and ends with '#'
     *
     * Returns a map of arguments
     */
    protected fun parseInlineArguments(text: String): Map<String, Any> {
        val arguments = HashMap<String, Any>()
        if (text.isNotEmpty()) {
            val pairs = text.split("&")
            for (pair in pairs) {
                val args = pair.split("=")
                if (args.size == 2) {
                    arguments[args[0]] = args[1]
                }
            }
        }

        return arguments
    }
}