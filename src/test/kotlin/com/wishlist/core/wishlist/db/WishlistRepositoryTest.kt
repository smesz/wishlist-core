package com.wishlist.core.wishlist.db

import com.wishlist.core.test.BaseRepositoryTest
import com.wishlist.core.test.TestObjects.aWishlistEntity
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import reactor.kotlin.test.test
import java.util.*

class WishlistRepositoryTest(@Autowired private val wishlistRepository: WishlistRepository) : BaseRepositoryTest() {

    @AfterEach
    fun tearDown() {
        wishlistRepository.deleteAll().block()
    }

    @Test
    @DisplayName("should find 2 wishlists by owner id")
    fun shouldFindWishlistByOwner() {
        wishlistRepository.saveAll(
            listOf(
                aWishlistEntity().copy(owner = UUID(0, 1), name = "expensive things"),
                aWishlistEntity().copy(owner = UUID(0, 1), name = "cheap things"),
                aWishlistEntity().copy(owner = UUID(0, 2), name = "video games")
            )
        ).thenMany(
            wishlistRepository.findByOwner(UUID(0, 1))
        ).test()
            .assertNext { it.name shouldBe "expensive things" }
            .assertNext { it.name shouldBe "cheap things" }
            .verifyComplete()
    }
}