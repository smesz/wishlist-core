package com.wishlist.core.platform

import org.springframework.boot.autoconfigure.EnableAutoConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.time.Clock

@Configuration
@EnableAutoConfiguration
class CommonConfiguration {

    @Bean
    fun clock(): Clock = Clock.systemUTC()

}