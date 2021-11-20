package com.wishlist.core.user

import com.wishlist.core.platform.logger
import com.wishlist.core.user.api.CreateUserRequest
import com.wishlist.core.user.api.CreateUserResponse
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import reactor.core.publisher.Mono

@RestController
@RequestMapping("/api/v1/user")
class UserController(
    private val userService: UserService
) {

    private val log by logger()

    @PostMapping
    fun createUser(@RequestBody createUserRequest: CreateUserRequest): Mono<ResponseEntity<CreateUserResponse>> {
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
}