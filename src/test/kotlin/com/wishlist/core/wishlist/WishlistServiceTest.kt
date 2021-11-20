package com.wishlist.core.wishlist

import com.wishlist.core.test.TestObjects.aWishlistEntity
import com.wishlist.core.test.TestObjects.aWishlistItemEntity
import com.wishlist.core.test.TestObjects.anUserEntity
import com.wishlist.core.user.UserRepository
import com.wishlist.core.wishlist.api.CreateWishlistItemRequest
import com.wishlist.core.wishlist.api.CreateWishlistRequest
import com.wishlist.core.wishlist.db.WishlistEntity
import com.wishlist.core.wishlist.db.WishlistItemEntity
import com.wishlist.core.wishlist.db.WishlistItemRepository
import com.wishlist.core.wishlist.db.WishlistRepository
import com.wishlist.core.wishlist.exception.UserNotFoundException
import com.wishlist.core.wishlist.exception.WishlistAlreadyExistsException
import com.wishlist.core.wishlist.exception.WishlistDoesNotBelongToTheUser
import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.should
import io.kotest.matchers.shouldBe
import io.mockk.clearMocks
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.kotlin.test.expectError
import reactor.kotlin.test.test
import java.util.*

class WishlistServiceTest : ShouldSpec({

    val userRepository = mockk<UserRepository>()
    val wishlistRepository = mockk<WishlistRepository>()
    val wishlistItemRepository = mockk<WishlistItemRepository>()
    val service = WishlistService(userRepository, wishlistRepository, wishlistItemRepository)

    afterTest {
        clearMocks(userRepository, wishlistRepository, wishlistItemRepository)
    }

    should("create a wishlist") {
        // given
        val request = CreateWishlistRequest(
            name = "electronics",
            items = listOf(
                CreateWishlistItemRequest(
                    name = "PS5",
                    description = "with blue ray drive"
                )
            )
        )

        // mocks
        every { userRepository.findByEmail("adam.smith@gmail.com") } returns Mono.just(
            anUserEntity().copy(
                id = UUID(
                    0,
                    1
                )
            )
        )
        every { wishlistRepository.findByOwner(UUID(0, 1)) } returns Flux.just(
            aWishlistEntity().copy(name = "cheap things"),
            aWishlistEntity().copy(name = "video games")
        )

        val wishlistEntitySlot = slot<WishlistEntity>()
        every { wishlistRepository.save(capture(wishlistEntitySlot)) } answers {
            Mono.just(
                wishlistEntitySlot.captured.copy(
                    id = UUID(0, 2)
                )
            )
        }

        val wishlistItemEntitiesSlot = slot<Flux<WishlistItemEntity>>()
        every { wishlistItemRepository.saveAll(capture(wishlistItemEntitiesSlot)) } answers { wishlistItemEntitiesSlot.captured }

        // expect
        service.createWishlist("adam.smith@gmail.com", request).test()
            .assertNext { it.name shouldBe "electronics" }
            .verifyComplete()

        // assert captured wishlist entity
        wishlistEntitySlot.captured should {
            it.name shouldBe "electronics"
            it.owner shouldBe UUID(0, 1)
        }

        // assert captured wishlist item entities
        wishlistItemEntitiesSlot.captured.test()
            .assertNext {
                it.name shouldBe "PS5"
                it.description shouldBe "with blue ray drive"
                it.wishlistId shouldBe UUID(0, 2)
            }
    }

    should("not create wishlist if already exists") {
        // given
        val request = CreateWishlistRequest(
            name = "electronics",
            items = listOf(
                CreateWishlistItemRequest(
                    name = "PS5",
                    description = "with blue ray drive"
                )
            )
        )

        // mocks
        every { userRepository.findByEmail("adam.smith@gmail.com") } returns Mono.just(
            anUserEntity().copy(
                id = UUID(
                    0,
                    1
                )
            )
        )
        every { wishlistRepository.findByOwner(UUID(0, 1)) } returns Flux.just(
            aWishlistEntity().copy(name = "cheap things"),
            aWishlistEntity().copy(name = "video games"),
            aWishlistEntity().copy(name = "electronics")
        )

        // expect
        service.createWishlist("adam.smith@gmail.com", request).test()
            .expectError(WishlistAlreadyExistsException::class)
    }

    should("not create wishlist if user does not exist") {
        // given
        val request = CreateWishlistRequest(
            name = "electronics",
            items = listOf(
                CreateWishlistItemRequest(
                    name = "PS5",
                    description = "with blue ray drive"
                )
            )
        )

        // mocks
        every { userRepository.findByEmail("adam.smith@gmail.com") } returns Mono.empty()

        // expect
        service.createWishlist("adam.smith@gmail.com", request).test()
            .expectError(UserNotFoundException::class)
    }

    should("get all wishlists") {
        // given
        every { userRepository.findByEmail("adam.smith@gmail.com") } returns Mono.just(
            anUserEntity().copy(
                id = UUID(
                    0,
                    1
                )
            )
        )
        every { wishlistRepository.findByOwner(any()) } returns Flux.just(
            aWishlistEntity().copy(id = UUID(0, 1), name = "electronics"),
            aWishlistEntity().copy(id = UUID(0, 2), name = "video games")
        )

        every { wishlistItemRepository.findByWishlistId(UUID(0, 1)) } returns Flux.empty()
        every { wishlistItemRepository.findByWishlistId(UUID(0, 2)) } returns Flux.just(
            aWishlistItemEntity().copy(name = "the last of us 2"),
            aWishlistItemEntity().copy(name = "guardians of the galaxy")
        )

        // expect
        service.getAllWishlists("adam.smith@gmail.com").test()
            .assertNext {
                it.wishlists shouldHaveSize 2

                it.wishlists[0] should { wishlist ->
                    wishlist.name shouldBe "electronics"
                    wishlist.items shouldHaveSize 0
                }

                it.wishlists[1] should { wishlist ->
                    wishlist.name shouldBe "video games"
                    wishlist.items shouldHaveSize 2

                    wishlist.items[0].name shouldBe "the last of us 2"
                    wishlist.items[1].name shouldBe "guardians of the galaxy"
                }
            }
            .verifyComplete()
    }

    should("add item to the wishlist") {
        // given
        val wishlistId = UUID(0, 2)
        val request = CreateWishlistItemRequest(
            name = "crash bandicoot 4"
        )

        // mocks
        every { userRepository.findByEmail("adam.smith@gmail.com") } returns Mono.just(
            anUserEntity().copy(
                id = UUID(
                    0,
                    1
                )
            )
        )
        every { wishlistRepository.findByOwner(UUID(0, 1)) } returns Flux.just(
            aWishlistEntity().copy(id = UUID(0, 1), name = "electronics"),
            aWishlistEntity().copy(id = UUID(0, 2), name = "video games")
        )

        val wishlistItemEntitySlot = slot<WishlistItemEntity>()
        every { wishlistItemRepository.save(capture(wishlistItemEntitySlot)) } answers {
            Mono.just(
                wishlistItemEntitySlot.captured
            )
        }

        // expect
        service.addItemToWishlist("adam.smith@gmail.com", wishlistId, request).test()
            .expectNextCount(1)
            .verifyComplete()

        // assert captured item
        wishlistItemEntitySlot.captured should {
            it.name shouldBe "crash bandicoot 4"
        }
    }

    should("not add item to the wishlist that does not belong to the user") {
        // given
        val wishlistId = UUID(0, 3)
        val request = CreateWishlistItemRequest(
            name = "crash bandicoot 4"
        )

        // mocks
        every { userRepository.findByEmail("adam.smith@gmail.com") } returns Mono.just(
            anUserEntity().copy(
                id = UUID(
                    0,
                    1
                )
            )
        )
        every { wishlistRepository.findByOwner(UUID(0, 1)) } returns Flux.just(
            aWishlistEntity().copy(id = UUID(0, 1), name = "electronics"),
            aWishlistEntity().copy(id = UUID(0, 2), name = "video games")
        )

        // expect
        service.addItemToWishlist("adam.smith@gmail.com", wishlistId, request).test()
            .expectError(WishlistDoesNotBelongToTheUser::class)
    }
})
