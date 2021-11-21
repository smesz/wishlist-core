package com.wishlist.core.wishlist

import com.wishlist.core.platform.logger
import com.wishlist.core.user.UserEntity
import com.wishlist.core.user.UserRepository
import com.wishlist.core.wishlist.api.*
import com.wishlist.core.wishlist.db.WishlistEntity
import com.wishlist.core.wishlist.db.WishlistItemEntity
import com.wishlist.core.wishlist.db.WishlistItemRepository
import com.wishlist.core.wishlist.db.WishlistRepository
import com.wishlist.core.wishlist.exception.UserNotFoundException
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

    private val log by logger()

    fun createWishlist(ownerEmail: String, createWishlistRequest: CreateWishlistRequest): Mono<WishlistEntity> {
        return findUser(ownerEmail).flatMap { user ->
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

    fun getWishlist(ownerEmail: String, wishlistId: UUID): Mono<WishlistDto> {
        return findUser(ownerEmail).flatMap { user ->
            wishlistRepository.findByOwnerAndId(user.id!!, wishlistId).flatMap { wishlist ->
                wishlistItemRepository.findByWishlistId(wishlistId).collectList().map { items ->
                    WishlistDto(
                        id = wishlist.id!!,
                        name = wishlist.name,
                        items = items.map {
                            WishlistItemDto(
                                name = it.name,
                                description = it.description
                            )
                        }
                    )
                }
            }
        }.doOnNext {
            log.debug("Returning wishlist $it")
        }
    }

    fun getAllWishlists(ownerEmail: String): Mono<WishlistResponseWrapper> {
        return findUser(ownerEmail).flatMapMany { user ->
            wishlistRepository.findByOwner(user.id!!).flatMap { wishlist ->
                wishlistItemRepository.findByWishlistId(wishlist.id!!).collectList().map { items ->
                    WishlistDto(
                        id = wishlist.id!!,
                        name = wishlist.name,
                        items = items.map {
                            WishlistItemDto(
                                name = it.name,
                                description = it.description
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
        return findUser(ownerEmail).flatMap { user ->
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
                            name = createWishlistItemRequest.name,
                            description = createWishlistItemRequest.description
                        )
                    ).doOnSuccess {
                        log.info("Added new item to the wishlist: {}", it)
                    }
                }
            }
        }
    }

    private fun findUser(email: String): Mono<UserEntity> {
        return userRepository.findByEmail(email)
            .switchIfEmpty(
                Mono.error(UserNotFoundException(email))
            )
    }

    private fun isValid(ownedWishlists: List<WishlistEntity>, createWishlistRequest: CreateWishlistRequest): Boolean {
        return !ownedWishlists.containsName(createWishlistRequest.name)
    }

    private fun persistWishlist(ownerId: UUID, wishlistName: String): Mono<WishlistEntity> {
        return wishlistRepository.save(
            WishlistEntity(
                owner = ownerId,
                name = wishlistName
            )
        ).doOnSuccess {
            log.info("Saved new wishlist: {}", it)
        }
    }

    private fun persistWishlistItems(
        wishlist: WishlistEntity,
        items: List<CreateWishlistItemRequest>
    ): Mono<WishlistEntity> {
        val wishlistItemEntitiesFlux = Flux.fromIterable(items)
            .map { itemToAdd ->
                WishlistItemEntity(
                    wishlistId = wishlist.id!!,
                    name = itemToAdd.name,
                    description = itemToAdd.description
                )
            }

        return wishlistItemRepository.saveAll(wishlistItemEntitiesFlux)
            .doOnNext {
                log.info("Saved new item {} to the wishlist", it)
            }
            .then(Mono.just(wishlist))
    }

    private fun List<WishlistEntity>.containsName(wishlistName: String): Boolean {
        return any { it.name == wishlistName }
    }
}