package com.wishlist.core.platform

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Configuration

@Configuration
class LoggerConfiguration

fun <R : Any> R.logger(): Lazy<Logger> {
    return lazy {
        LoggerFactory.getLogger(this.javaClass)
    }
}