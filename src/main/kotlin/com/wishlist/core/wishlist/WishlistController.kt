package com.wishlist.core.wishlist

import com.wishlist.core.platform.logger
import com.wishlist.core.wishlist.api.CreateWishlistItemRequest
import com.wishlist.core.wishlist.api.CreateWishlistRequest
import com.wishlist.core.wishlist.api.WishlistResponseWrapper
import com.wishlist.core.wishlist.exception.WishlistAlreadyExistsException
import com.wishlist.core.wishlist.exception.WishlistDoesNotBelongToTheUser
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import reactor.core.publisher.Mono
import java.security.Principal
import java.util.*

@RestController
@RequestMapping("/api/v1/wishlist")
class WishlistController(
    private val wishlistService: WishlistService
) {

    private val log by logger()

    @PostMapping
    fun createWishlist(
        principal: Principal,
        @RequestBody createWishlistRequest: CreateWishlistRequest
    ): Mono<ResponseEntity<Void>> {
        log.debug("Got POST /api/v1/wishlist request with payload: {}", createWishlistRequest)

        return wishlistService.createWishlist(principal.name, createWishlistRequest)
            .map {
                ResponseEntity(HttpStatus.CREATED)
            }
    }

    @GetMapping("/all")
    fun getAllWishlists(principal: Principal): Mono<ResponseEntity<WishlistResponseWrapper>> {
        log.debug("Got GET /api/v1/wishlist/all")

        return wishlistService.getAllWishlists(principal.name)
            .map {
                ResponseEntity.ok(it)
            }
    }

    @PutMapping("/{wishlistId}")
    fun putItemToWishlist(
        principal: Principal,
        @PathVariable("wishlistId") wishlistId: UUID,
        @RequestBody createWishlistItemRequest: CreateWishlistItemRequest
    ): Mono<ResponseEntity<Void>> {
        log.debug("Got PUT /api/v1/user/wishlist/$wishlistId with payload: {}", createWishlistItemRequest)

        return wishlistService.addItemToWishlist(principal.name, wishlistId, createWishlistItemRequest)
            .map {
                ResponseEntity.noContent().build()
            }
    }

    @ExceptionHandler(value = [WishlistAlreadyExistsException::class, WishlistDoesNotBelongToTheUser::class])
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    fun badRequestExceptionHandler(ex: Exception): String? {
        return ex.message
    }
}