package com.wishlist.core.wishlist.exception

class UserNotFoundException(email: String) : RuntimeException("User $email not found")