package com.wishlist.core.wishlist.db

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Table
import java.util.*

@Table("wishlist")
data class WishlistEntity(

    @Id
    val id: UUID? = null,

    val owner: UUID,
    var name: String
)