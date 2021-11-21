package com.wishlist.core.wishlist

import com.ninjasquad.springmockk.MockkBean
import com.wishlist.core.test.TestObjects.aWishlistDto
import com.wishlist.core.test.TestObjects.aWishlistEntity
import com.wishlist.core.test.TestObjects.aWishlistItemEntity
import com.wishlist.core.test.TestObjects.aWishlistResponseWrapper
import com.wishlist.core.wishlist.api.CreateWishlistItemRequest
import com.wishlist.core.wishlist.api.CreateWishlistRequest
import com.wishlist.core.wishlist.exception.WishlistAlreadyExistsException
import io.mockk.every
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest
import org.springframework.http.MediaType
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers.csrf
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.reactive.server.WebTestClient
import reactor.core.publisher.Mono
import java.util.*

@ActiveProfiles("test")
@WebFluxTest(WishlistController::class)
internal class WishlistControllerTest {

    @MockkBean
    lateinit var wishlistService: WishlistService

    @Autowired
    lateinit var webTestClient: WebTestClient

    @Test
    @DisplayName("should call POST /api/v1/wishlist and create new wishlist")
    @WithMockUser(username = "adam.smith@gmail.com")
    fun shouldCreateWishlist() {
        // given
        val email = "adam.smith@gmail.com"
        val wishlistId = UUID.randomUUID()

        // mocks
        every {
            wishlistService.createWishlist(email, any())
        } returns Mono.just(aWishlistEntity().copy(id = wishlistId))

        every { wishlistService.getWishlist(email, wishlistId) } returns Mono.just(aWishlistDto())

        // when
        val response = webTestClient
            .mutateWith(csrf())
            .post()
            .uri("/api/v1/wishlist")
            .contentType(MediaType.APPLICATION_JSON)
            .body(
                Mono.just(
                    CreateWishlistRequest(
                        name = "electronics",
                        items = listOf(
                            CreateWishlistItemRequest("32GB RAM", "for better programming experience"),
                            CreateWishlistItemRequest("new mouse Logitech", "X34-1 model")
                        )
                    )
                ), CreateWishlistRequest::class.java
            )
            .exchange()

        // then
        response.expectStatus().isCreated
    }

    @Test
    @DisplayName("should call POST /api/v1/wishlist and react to exception thrown")
    @WithMockUser(username = "adam.smith@gmail.com")
    fun shouldNotCreateWishlistWhenExceptionIsThrown() {
        // mocks
        every {
            wishlistService.createWishlist(
                "adam.smith@gmail.com",
                any()
            )
        } returns Mono.error(WishlistAlreadyExistsException("electronics"))

        // when
        val response = webTestClient
            .mutateWith(csrf())
            .post()
            .uri("/api/v1/wishlist")
            .contentType(MediaType.APPLICATION_JSON)
            .body(
                Mono.just(
                    CreateWishlistRequest(
                        name = "electronics",
                        items = listOf(
                            CreateWishlistItemRequest("32GB RAM", "for better programming experience"),
                            CreateWishlistItemRequest("new mouse Logitech", "X34-1 model")
                        )
                    )
                ), CreateWishlistRequest::class.java
            )
            .exchange()

        // then
        response.expectStatus().isBadRequest
    }

    @Test
    @DisplayName("should call PUT /api/v1/wishlist/{id} to place new item in the wishlist")
    @WithMockUser(username = "adam.smith@gmail.com")
    fun shouldPutItemToWishlist() {
        // mocks
        val wishlistId = UUID.randomUUID()
        every {
            wishlistService.addItemToWishlist(
                "adam.smith@gmail.com",
                wishlistId,
                any()
            )
        } returns Mono.just(aWishlistItemEntity())

        // when
        val response = webTestClient
            .mutateWith(csrf())
            .put()
            .uri("/api/v1/wishlist/$wishlistId")
            .contentType(MediaType.APPLICATION_JSON)
            .body(
                Mono.just(CreateWishlistItemRequest("32GB RAM", "for better programming experience")),
                CreateWishlistItemRequest::class.java
            )
            .exchange()

        // then
        response.expectStatus().isNoContent
    }

    @Test
    @DisplayName("should call GET /api/v1/wishlist/all to obtain all wishlists for logged in user")
    @WithMockUser(username = "adam.smith@gmail.com")
    fun shouldGetAllWishlists() {
        // mocks
        val wishlistId = UUID.randomUUID()
        every {
            wishlistService.getAllWishlists("adam.smith@gmail.com")
        } returns Mono.just(aWishlistResponseWrapper())

        // when
        val response = webTestClient
            .mutateWith(csrf())
            .get()
            .uri("/api/v1/wishlist/all")
            .exchange()

        // then
        response.expectStatus().isOk
            .expectBody().json(
                """
                {
                  "wishlists": [
                    {
                      "name": "electronics",
                      "items": [
                        {
                          "name": "PS5",
                          "description": "with blue-ray drive"
                        },
                        {
                          "name": "Xbox series x",
                          "description": "with game pass subscription"
                        }
                      ]
                    }
                  ]
                }
            """.trimIndent()
            )
    }

    @Test
    @DisplayName("should call GET /api/v1/wishlist/{id} to obtain given wishlist for logged in user")
    @WithMockUser(username = "adam.smith@gmail.com")
    fun shouldGetGivenWishlists() {
        // mocks
        val wishlistId = UUID.randomUUID()
        every {
            wishlistService.getWishlist("adam.smith@gmail.com", wishlistId)
        } returns Mono.just(aWishlistDto())

        // when
        val response = webTestClient
            .mutateWith(csrf())
            .get()
            .uri("/api/v1/wishlist/$wishlistId")
            .exchange()

        // then
        response.expectStatus().isOk
            .expectBody().json(
                """
                {
                   "name": "electronics",
                   "items": [
                     {
                       "name": "PS5",
                       "description": "with blue-ray drive"
                     },
                     {
                       "name": "Xbox series x",
                       "description": "with game pass subscription"
                     }
                    ]
                }
            """.trimIndent()
            )
    }
}