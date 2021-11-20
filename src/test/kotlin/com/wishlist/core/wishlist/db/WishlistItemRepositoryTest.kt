package com.wishlist.core.wishlist.db

import com.wishlist.core.test.BaseRepositoryTest
import com.wishlist.core.test.TestObjects.aWishlistItemEntity
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import reactor.kotlin.test.test
import java.util.*

class WishlistItemRepositoryTest(@Autowired private val wishlistItemRepository: WishlistItemRepository) :
    BaseRepositoryTest() {

    @AfterEach
    fun tearDown() {
        wishlistItemRepository.deleteAll().block()
    }

    @Test
    @DisplayName("should find items by wishlist id")
    fun shouldFindItemsByWishlistId() {
        wishlistItemRepository.saveAll(
            listOf(
                aWishlistItemEntity().copy(wishlistId = UUID(0, 1), name = "Sony PS5"),
                aWishlistItemEntity().copy(wishlistId = UUID(0, 1), name = "XBOX Series S"),
                aWishlistItemEntity().copy(wishlistId = UUID(0, 2), name = "Braun shaving machine")
            )
        ).thenMany(
            wishlistItemRepository.findByWishlistId(UUID(0, 1))
        ).test()
            .assertNext { it.name shouldBe "Sony PS5" }
            .assertNext { it.name shouldBe "XBOX Series S" }
            .verifyComplete()
    }
}