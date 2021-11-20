package com.wishlist.core.user

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Table
import java.time.Instant
import java.time.LocalDate
import java.util.*

@Table("app_user")
data class UserEntity(

    @Id
    val id: UUID? = null,

    val email: String,
    var password: String,
    var roles: MutableSet<String>,
    var userHash: String,

    var firstName: String,
    var lastName: String,
    val birthday: LocalDate,

    var registeredAt: Instant,
    var updatedAt: Instant
)