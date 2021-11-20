package com.wishlist.core.wishlist.exception

import java.util.*

class WishlistDoesNotBelongToTheUser(wishlistId: UUID, email: String) :
    RuntimeException("Wishlist with id '$wishlistId' does not belong to the user with email '$email'")