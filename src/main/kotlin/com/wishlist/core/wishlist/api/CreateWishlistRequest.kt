package com.wishlist.core.wishlist.api

data class CreateWishlistRequest(
    val name: String,
    val items: List<CreateWishlistItemRequest> = emptyList()
)