package com.wishlist.core.user

import org.springframework.data.repository.reactive.ReactiveCrudRepository
import reactor.core.publisher.Mono
import java.util.*

interface UserRepository : ReactiveCrudRepository<UserEntity, UUID> {

    fun findByEmail(email: String): Mono<UserEntity>
}