package com.wishlist.core.test

import org.springframework.boot.test.autoconfigure.data.r2dbc.DataR2dbcTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.TestPropertySource


@ActiveProfiles("test")
@DataR2dbcTest
@ContextConfiguration(
    classes = [RepositoriesTestConfiguration::class],
    initializers = [RepositoriesTestConfiguration.Initializer::class]
)
@TestPropertySource(
    properties = [
        "spring.main.web-application-type=none"
    ]
)
open class BaseRepositoryTest