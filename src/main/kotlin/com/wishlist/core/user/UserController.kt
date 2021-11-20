package com.wishlist.core.user

import com.wishlist.core.platform.logger
import com.wishlist.core.user.api.CreateUserRequest
import com.wishlist.core.user.api.CreateUserResponse
import com.wishlist.core.user.exception.UserAlreadyRegisteredException
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import reactor.core.publisher.Mono

@RestController
@RequestMapping("/api/v1/user")
class UserController(
    private val userService: UserService
) {

    private val log by logger()

    @PostMapping
    fun createUser(@RequestBody createUserRequest: CreateUserRequest): Mono<ResponseEntity<Any>> {
        log.debug("Got POST /api/v1/user request with payload: {}", createUserRequest)

        return userService.createUser(createUserRequest)
            .map {
                CreateUserResponse(
                    firstName = it.firstName,
                    lastName = it.lastName,
                    hash = it.userHash
                )
            }
            .map { ResponseEntity(it, HttpStatus.CREATED) }
    }

    @ExceptionHandler(UserAlreadyRegisteredException::class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    fun badRequestExceptionHandler(ex: Exception): String? {
        return ex.message
    }
}