package com.wishlist.core

import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.util.TestPropertyValues
import org.springframework.context.ApplicationContextInitializer
import org.springframework.context.ConfigurableApplicationContext
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.ContextConfiguration
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers

@ActiveProfiles("test")
@SpringBootTest
@Testcontainers
@ContextConfiguration(initializers = [CoreApplicationTests.Initializer::class])
class CoreApplicationTests {

    companion object {
        @Container
        val postgreSQLContainer = PostgreSQLContainer<Nothing>("postgres:9.6.12").apply {
            withDatabaseName("db")
            withUsername("WISHLIST_CORE_USER")
            withPassword("WISHLIST_CORE_PASS")
            withInitScript("testdb-init-uuid.sql")
        }

        @BeforeAll
        @JvmStatic
        fun setup() {
            postgreSQLContainer.start()
        }

        @AfterAll
        @JvmStatic
        fun cleanup() {
            postgreSQLContainer.stop()
        }
    }

    class Initializer : ApplicationContextInitializer<ConfigurableApplicationContext> {
        override fun initialize(configurableApplicationContext: ConfigurableApplicationContext) {
            postgreSQLContainer.start()
            TestPropertyValues.of(
                "spring.r2dbc.url=" + postgreSQLContainer.jdbcUrl.replace("jdbc", "r2dbc"),
                "spring.flyway.url=" + postgreSQLContainer.jdbcUrl
            ).applyTo(configurableApplicationContext.environment)
        }
    }

    @Test
    fun contextLoads() {
    }

}
