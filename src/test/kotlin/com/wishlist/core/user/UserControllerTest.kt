package com.wishlist.core.user

import com.ninjasquad.springmockk.MockkBean
import com.wishlist.core.test.TestObjects
import com.wishlist.core.user.api.CreateUserRequest
import com.wishlist.core.user.exception.UserAlreadyRegisteredException
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
import java.time.LocalDate

@WebFluxTest(UserController::class)
@ActiveProfiles("test")
internal class UserControllerTest {

    @MockkBean
    lateinit var userService: UserService

    @Autowired
    lateinit var webTestClient: WebTestClient

    @Test
    @DisplayName("should call POST /api/v1/user and register new user")
    @WithMockUser
    fun shouldCreateUser() {
        // mocks
        every { userService.createUser(any()) } returns Mono.just(TestObjects.anUserEntity())

        // when
        val response = webTestClient
            .mutateWith(csrf())
            .post()
            .uri("/api/v1/user")
            .contentType(MediaType.APPLICATION_JSON)
            .body(
                Mono.just(
                    CreateUserRequest(
                        firstName = "Adam",
                        lastName = "Smith",
                        "adam.smith@gmail.com",
                        "password",
                        birthday = LocalDate.of(1991, 10, 3)
                    )
                ), CreateUserRequest::class.java
            )
            .exchange()

        // then
        response.expectStatus().isCreated
            .expectBody().json(
                """
                {
                  "firstName": "Adam",
                  "lastName": "Smith",
                  "hash" : "RA3gqa2"
                }
            """.trimIndent()
            )
    }

    @Test
    @DisplayName("should call POST /api/v1/user and return 5xx when error occured")
    @WithMockUser
    fun shouldNotCreateUser() {
        // mocks
        every { userService.createUser(any()) } returns Mono.error(UserAlreadyRegisteredException("adam.smith@gmail.com"))

        // when
        val response = webTestClient
            .mutateWith(csrf())
            .post()
            .uri("/api/v1/user")
            .contentType(MediaType.APPLICATION_JSON)
            .body(
                Mono.just(
                    CreateUserRequest(
                        firstName = "Adam",
                        lastName = "Smith",
                        "adam.smith@gmail.com",
                        "password",
                        birthday = LocalDate.of(1991, 10, 3)
                    )
                ), CreateUserRequest::class.java
            )
            .exchange()

        // then
        response.expectStatus().isBadRequest
    }
}