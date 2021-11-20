package com.wishlist.core.wishlist.api

data class CreateWishlistItemRequest(
    val name: String,
    val description: String? = null
)