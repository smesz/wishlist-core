package com.wishlist.core.wishlist.exception

class WishlistAlreadyExistsException(name: String) : RuntimeException("Wishlist '$name' already exists")