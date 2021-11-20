package com.wishlist.core.user

import com.wishlist.core.platform.logger
import com.wishlist.core.user.api.CreateUserRequest
import com.wishlist.core.user.api.CreateUserResponse
import com.wishlist.core.user.api.PutObjectRequest
import com.wishlist.core.wishlist.WishlistService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import reactor.core.publisher.Mono
import java.security.Principal

@RestController
@RequestMapping("/api/v1/user")
class UserController(
    private val userService: UserService,
    private val wishlistService: WishlistService
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

    @GetMapping("/wishlist")
    fun getCurrentUserWishlist(principal: Principal) {
        log.debug("Got GET /api/v1/user/wishlist")

        userService.getWishlist(principal.name)
    }

    @PutMapping("/wishlist")
    fun putItemToWishlist(principal: Principal, @RequestBody putObjectRequest: PutObjectRequest) {
        log.debug("Got PUT /api/v1/user/wishlist with payload: {}", putObjectRequest)

        wishlistService.addObjectToWishlist(principal.name, putObjectRequest)
    }

//    @PostMapping()
//    fun addItemToWishlist(): Mono<ResponseEntity<Any>> {
//        log.debug()
//
//    }

}