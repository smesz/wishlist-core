package com.wishlist.core.wishlist

import com.wishlist.core.user.UserRepository
import com.wishlist.core.wishlist.api.*
import com.wishlist.core.wishlist.db.WishlistEntity
import com.wishlist.core.wishlist.db.WishlistItemEntity
import com.wishlist.core.wishlist.db.WishlistItemRepository
import com.wishlist.core.wishlist.db.WishlistRepository
import com.wishlist.core.wishlist.exception.WishlistAlreadyExistsException
import com.wishlist.core.wishlist.exception.WishlistDoesNotBelongToTheUser
import org.springframework.stereotype.Service
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.util.*

@Service
class WishlistService(
    private val userRepository: UserRepository,
    private val wishlistRepository: WishlistRepository,
    private val wishlistItemRepository: WishlistItemRepository
) {

    fun createWishlist(ownerEmail: String, createWishlistRequest: CreateWishlistRequest): Mono<WishlistEntity> {
        return userRepository.findByEmail(ownerEmail).flatMap { user ->
            wishlistRepository.findByOwner(user.id!!).collectList().flatMap { ownedWishlists ->
                if (!isValid(ownedWishlists, createWishlistRequest)) {
                    Mono.error(WishlistAlreadyExistsException(createWishlistRequest.name))
                } else {
                    persistWishlist(user.id, createWishlistRequest.name)
                        .flatMap { wishlist ->
                            persistWishlistItems(wishlist, createWishlistRequest.items)
                        }
                }
            }
        }
    }

    fun getAllWishlists(ownerEmail: String): Mono<WishlistResponseWrapper> {
        return userRepository.findByEmail(ownerEmail).flatMapMany { user ->
            wishlistRepository.findByOwner(user.id!!).flatMap { wishlist ->
                wishlistItemRepository.findByWishlistId(wishlist.id!!).collectList().map { items ->
                    WishlistDto(
                        name = wishlist.name,
                        items = items.map {
                            WishlistItemDto(
                                name = it.itemName,
                                description = it.itemDescription
                            )
                        }
                    )
                }
            }
        }.collectList()
            .map {
                WishlistResponseWrapper(
                    wishlists = it
                )
            }
    }

    fun addItemToWishlist(
        ownerEmail: String,
        wishlistId: UUID,
        createWishlistItemRequest: CreateWishlistItemRequest
    ): Mono<WishlistItemEntity> {

        // check if the wishlistId actually belongs to the calling user
        return userRepository.findByEmail(ownerEmail).flatMap { user ->
            wishlistRepository.findByOwner(user.id!!).collectList().map { allWishlists ->
                allWishlists.any { it.id == wishlistId }
            }
        }.flatMap { wishlistBelongsToTheUser ->
            when (wishlistBelongsToTheUser) {
                false -> Mono.error(WishlistDoesNotBelongToTheUser(wishlistId, ownerEmail))
                true -> {
                    wishlistItemRepository.save(
                        WishlistItemEntity(
                            wishlistId = wishlistId,
                            itemName = createWishlistItemRequest.name,
                            itemDescription = createWishlistItemRequest.description
                        )
                    )
                }
            }
        }
    }

    private fun isValid(ownedWishlists: List<WishlistEntity>, createWishlistRequest: CreateWishlistRequest): Boolean {
        return ownedWishlists.containsName(createWishlistRequest.name)
    }

    private fun persistWishlist(ownerId: UUID, wishlistName: String): Mono<WishlistEntity> {
        return wishlistRepository.save(
            WishlistEntity(
                owner = ownerId,
                name = wishlistName
            )
        )
    }

    private fun persistWishlistItems(
        wishlist: WishlistEntity,
        items: List<CreateWishlistItemRequest>
    ): Mono<WishlistEntity> {
        val wishlistItemEntitiesFlux = Flux.fromIterable(items)
            .map { itemToAdd ->
                WishlistItemEntity(
                    wishlistId = wishlist.id!!,
                    itemName = itemToAdd.name,
                    itemDescription = itemToAdd.description
                )
            }

        return wishlistItemRepository.saveAll(wishlistItemEntitiesFlux)
            .then(Mono.just(wishlist))
    }

    private fun List<WishlistEntity>.containsName(wishlistName: String): Boolean {
        return any { it.name == wishlistName }
    }
}