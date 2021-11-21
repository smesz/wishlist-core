package com.wishlist.core.wishlist.api

import java.util.*

data class WishlistResponseWrapper(
    val wishlists: List<WishlistDto>
)

data class WishlistDto(
    val id: UUID,
    val name: String,
    val items: List<WishlistItemDto>
)

data class WishlistItemDto(
    val name: String,
    val description: String? = null
)