package com.wishlist.core.user.exception

class UserAlreadyRegisteredException(email: String) : RuntimeException("User $email already registered")