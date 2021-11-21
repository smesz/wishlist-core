package com.wishlist.core.wishlist.db

import org.springframework.data.repository.reactive.ReactiveCrudRepository
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.util.*

interface WishlistRepository : ReactiveCrudRepository<WishlistEntity, UUID> {

    fun findByOwner(owner: UUID): Flux<WishlistEntity>
    fun findByOwnerAndId(owner: UUID, id: UUID): Mono<WishlistEntity>
}