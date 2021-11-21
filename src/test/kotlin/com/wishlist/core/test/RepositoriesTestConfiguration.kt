package com.wishlist.core.test

import com.wishlist.core.platform.StringSetReadingConverter
import com.wishlist.core.platform.StringSetWritingConverter
import io.r2dbc.spi.ConnectionFactory
import org.springframework.beans.factory.DisposableBean
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.boot.test.util.TestPropertyValues
import org.springframework.context.ApplicationContextInitializer
import org.springframework.context.ConfigurableApplicationContext
import org.springframework.context.annotation.Bean
import org.springframework.data.r2dbc.config.AbstractR2dbcConfiguration
import org.springframework.data.r2dbc.repository.config.EnableR2dbcRepositories
import org.testcontainers.containers.PostgreSQLContainer

@TestConfiguration
@EnableR2dbcRepositories(basePackages = ["com.wishlist.core"])
class RepositoriesTestConfiguration : AbstractR2dbcConfiguration() {

    companion object {
        private val postgreSQLContainer = PostgreSQLContainer<Nothing>("postgres:9.6.12").apply {
            withDatabaseName("db")
            withUsername("WISHLIST_CORE_USER")
            withPassword("WISHLIST_CORE_PASS")
            withInitScript("testdb-init-uuid.sql")
        }
    }

    @Autowired
    lateinit var connectionFactory: ConnectionFactory

    @Bean
    fun shutdownableTestDatabase(): ShutdownableTestDatabase {
        return ShutdownableTestDatabase(postgreSQLContainer)
    }

    override fun getCustomConverters(): MutableList<Any> {
        return mutableListOf(
            StringSetReadingConverter(),
            StringSetWritingConverter()
        )
    }

    class Initializer : ApplicationContextInitializer<ConfigurableApplicationContext> {
        override fun initialize(ctx: ConfigurableApplicationContext) {
            postgreSQLContainer.start()
            TestPropertyValues.of(
                "spring.r2dbc.url=" + postgreSQLContainer.jdbcUrl.replace("jdbc", "r2dbc"),
                "spring.flyway.url=" + postgreSQLContainer.jdbcUrl
            ).applyTo(ctx.environment)
        }
    }

    override fun connectionFactory() = connectionFactory
}


class ShutdownableTestDatabase(private val postgreSQLContainer: PostgreSQLContainer<Nothing>) : DisposableBean {

    override fun destroy() {
        postgreSQLContainer.stop()
    }
}