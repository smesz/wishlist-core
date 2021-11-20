package com.wishlist.core.test

import com.wishlist.core.user.UserEntity
import com.wishlist.core.wishlist.api.WishlistDto
import com.wishlist.core.wishlist.api.WishlistItemDto
import com.wishlist.core.wishlist.api.WishlistResponseWrapper
import com.wishlist.core.wishlist.db.WishlistEntity
import com.wishlist.core.wishlist.db.WishlistItemEntity
import java.time.Instant
import java.time.LocalDate
import java.util.*

object TestObjects {

    fun anUserEntity() = UserEntity(
        firstName = "Adam",
        lastName = "Smith",
        email = "adam.smith@gmail.com",
        password = "password",
        roles = mutableSetOf("BASIC_USER"),
        userHash = "RA3gqa2",
        birthday = LocalDate.of(1991, 10, 3),
        registeredAt = Instant.now(),
        updatedAt = Instant.now()
    )

    fun aWishlistEntity() = WishlistEntity(
        owner = UUID.randomUUID(),
        name = "wishlist name"
    )

    fun aWishlistItemEntity() = WishlistItemEntity(
        wishlistId = UUID.randomUUID(),
        name = "Sony Playstation 5",
        description = "with blue-ray!"
    )

    fun aWishlistResponseWrapper() = WishlistResponseWrapper(
        wishlists = listOf(
            WishlistDto(
                name = "electronics",
                items = listOf(
                    WishlistItemDto(
                        name = "PS5",
                        description = "with blue-ray drive"
                    ),
                    WishlistItemDto(
                        name = "Xbox series x",
                        description = "with game pass subscription"
                    )
                )
            )
        )
    )
}