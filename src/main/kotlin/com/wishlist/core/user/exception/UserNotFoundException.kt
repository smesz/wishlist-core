package com.wishlist.core.user.exception

class UserNotFoundException(email: String) : RuntimeException("User $email not found")