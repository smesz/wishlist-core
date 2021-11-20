package com.wishlist.core.user.api

import java.time.LocalDate

data class CreateUserRequest(

    val firstName: String,
    val lastName: String,
    val email: String,
    val password: String,

    val birthday: LocalDate
)