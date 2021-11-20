package com.wishlist.core.test

import com.wishlist.core.user.UserEntity
import com.wishlist.core.wishlist.db.WishlistEntity
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
}