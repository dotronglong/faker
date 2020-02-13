package com.dotronglong.faker.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.stereotype.Component

@Component
@ConfigurationProperties(prefix = "faker")
data class FakerApplicationProperties(
        var source: String = "",
        var watch: Boolean = false,
        var version: String = "dev"
)