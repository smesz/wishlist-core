package com.wishlist.core.wishlist.api

data class WishlistResponseWrapper(
    val wishlists: List<WishlistDto>
)

data class WishlistDto(
    val name: String,
    val items: List<WishlistItemDto>
)

data class WishlistItemDto(
    val name: String,
    val description: String?
)