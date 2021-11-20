package com.wishlist.core.wishlist.db

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Table
import java.util.*

@Table("wishlist_item")
data class WishlistItemEntity(

    @Id
    val id: UUID? = null,

    val wishlistId: UUID,
    val itemName: String,
    val itemDescription: String? = null
)