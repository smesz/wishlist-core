package com.wishlist.core.platform

import io.r2dbc.spi.ConnectionFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Configuration
import org.springframework.data.r2dbc.config.AbstractR2dbcConfiguration
import org.springframework.data.r2dbc.repository.config.EnableR2dbcRepositories

@Configuration
@EnableR2dbcRepositories(basePackages = ["com.wishlist.core"])
class DatabaseConfiguration : AbstractR2dbcConfiguration() {

    @Autowired
    lateinit var connectionFactory: ConnectionFactory

    override fun connectionFactory(): ConnectionFactory {
        return connectionFactory
    }

    override fun getCustomConverters(): MutableList<Any> {
        return mutableListOf(
            StringSetReadingConverter(),
            StringSetWritingConverter()
        )
    }
}