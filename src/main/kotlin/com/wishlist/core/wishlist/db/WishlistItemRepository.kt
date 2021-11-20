package com.wishlist.core.wishlist.db

import org.springframework.data.repository.reactive.ReactiveCrudRepository
import reactor.core.publisher.Flux
import java.util.*

interface WishlistItemRepository : ReactiveCrudRepository<WishlistItemEntity, UUID> {

    fun findByWishlistId(wishlistId: UUID): Flux<WishlistItemEntity>
}