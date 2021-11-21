package com.wishlist.core

import com.wishlist.core.platform.logger
import com.wishlist.core.user.api.CreateUserRequest
import com.wishlist.core.user.api.CreateUserResponse
import com.wishlist.core.wishlist.api.CreateWishlistItemRequest
import com.wishlist.core.wishlist.api.CreateWishlistRequest
import com.wishlist.core.wishlist.api.WishlistDto
import com.wishlist.core.wishlist.api.WishlistResponseWrapper
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.should
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.*
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.util.TestPropertyValues
import org.springframework.boot.web.server.LocalServerPort
import org.springframework.context.ApplicationContextInitializer
import org.springframework.context.ConfigurableApplicationContext
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.ContextConfiguration
import org.springframework.web.reactive.function.client.WebClient
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import reactor.core.publisher.Mono
import reactor.kotlin.test.test
import java.time.LocalDate
import java.util.*


@ActiveProfiles("test")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
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

    @LocalServerPort
    var randomServerPort: Int = 0

    private lateinit var webClient: WebClient

    private val log by logger()

    @BeforeEach
    fun beforeEach() {
        webClient = WebClient.builder()
            .baseUrl("http://localhost:$randomServerPort/api/v1/")
            .build()
    }

    @Test
    fun contextLoads() {
    }

    @Test
    @DisplayName("should create user, a wishlist and add 2 items")
    fun basicWorkflow() {
        val auth = Pair("krzysztof@gmail.com", "password")
        createUser(
            CreateUserRequest(
                firstName = "Krzysztof",
                lastName = "Kowalski",
                email = "krzysztof@gmail.com",
                password = "password",
                birthday = LocalDate.of(1987, 10, 29)
            )
        ).then(
            addWishlist(CreateWishlistRequest(name = "video games"), auth).flatMap { wishlist ->
                val wishlistId = wishlist.id

                addItem(wishlistId, CreateWishlistItemRequest(name = "Hades", description = "PL lang version"), auth)
                    .then(addItem(wishlistId, CreateWishlistItemRequest(name = "FIST"), auth))
                    .then(getWishlist(wishlistId, auth))
            }
        ).test()
            .assertNext { wishlist ->
                wishlist.name shouldBe "video games"
                wishlist.items shouldHaveSize 2
                wishlist.items.map { it.name } shouldContainExactly listOf("Hades", "FIST")
            }
            .verifyComplete()
    }

    @Test
    @DisplayName("should create a user, 2 wishlists and populate items to each")
    fun basicWorkflowWithTwoWishlists() {
        val auth = Pair("andrew@gmail.com", "password")
        createUser(
            CreateUserRequest(
                firstName = "Andrew",
                lastName = "Smith",
                email = "andrew@gmail.com",
                password = "password",
                birthday = LocalDate.of(1987, 10, 29)
            )
        ).then(
            addWishlist(CreateWishlistRequest(name = "video games"), auth).flatMap { wishlist ->
                val wishlistId = wishlist.id

                addItem(wishlistId, CreateWishlistItemRequest(name = "Hades", description = "PL lang version"), auth)
                    .then(addItem(wishlistId, CreateWishlistItemRequest(name = "FIST"), auth))
            }
        ).then(
            addWishlist(CreateWishlistRequest(name = "toys for kids"), auth).flatMap { wishlist ->
                val wishlistId = wishlist.id

                addItem(wishlistId, CreateWishlistItemRequest(name = "Lego Duplo train", description = "huge"), auth)
                    .then(addItem(wishlistId, CreateWishlistItemRequest(name = "Small animals"), auth))
                    .then(addItem(wishlistId, CreateWishlistItemRequest(name = "Toy soldiers"), auth))
            }
        ).then(getAllWishlists(auth))
            .test()
            .assertNext {
                it.wishlists shouldHaveSize 2
                it.wishlists[0] should { wishlist ->
                    wishlist.name shouldBe "video games"
                    wishlist.items shouldHaveSize 2
                }

                it.wishlists[1] should { wishlist ->
                    wishlist.name shouldBe "toys for kids"
                    wishlist.items shouldHaveSize 3
                }

            }
            .verifyComplete()
    }

    private fun createUser(request: CreateUserRequest): Mono<CreateUserResponse> {
        return webClient
            .post()
            .uri("/user")
            .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .body(Mono.just(request), request::class.java)
            .retrieve()
            .bodyToMono(CreateUserResponse::class.java)
    }

    private fun addWishlist(request: CreateWishlistRequest, auth: Pair<String, String>): Mono<WishlistDto> {
        return webClient.post()
            .uri("/wishlist")
            .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .headers { it.setBasicAuth(auth.first, auth.second) }
            .body(Mono.just(request), CreateWishlistRequest::class.java)
            .retrieve()
            .bodyToMono(WishlistDto::class.java)
    }

    private fun addItem(wishlistId: UUID, request: CreateWishlistItemRequest, auth: Pair<String, String>): Mono<Void> {
        return webClient.put()
            .uri("/wishlist/$wishlistId")
            .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .headers { it.setBasicAuth(auth.first, auth.second) }
            .body(Mono.just(request), CreateWishlistItemRequest::class.java)
            .retrieve()
            .bodyToMono(Void::class.java)
    }

    private fun getWishlist(wishlistId: UUID, auth: Pair<String, String>): Mono<WishlistDto> {
        return webClient.get()
            .uri("/wishlist/$wishlistId")
            .headers { it.setBasicAuth(auth.first, auth.second) }
            .retrieve()
            .bodyToMono(WishlistDto::class.java)
    }

    private fun getAllWishlists(auth: Pair<String, String>): Mono<WishlistResponseWrapper> {
        return webClient.get()
            .uri("/wishlist/all")
            .headers { it.setBasicAuth(auth.first, auth.second) }
            .retrieve()
            .bodyToMono(WishlistResponseWrapper::class.java)
    }
}
