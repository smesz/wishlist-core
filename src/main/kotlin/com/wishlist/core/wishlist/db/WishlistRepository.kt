package com.wishlist.core.wishlist.db

import org.springframework.data.repository.reactive.ReactiveCrudRepository
import reactor.core.publisher.Flux
import java.util.*

interface WishlistRepository : ReactiveCrudRepository<WishlistEntity, UUID> {

    fun findByOwner(owner: UUID): Flux<WishlistEntity>
}