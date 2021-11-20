package com.wishlist.core.user.api

data class CreateUserResponse(
    val firstName: String,
    val lastName: String,
    val hash: String
)