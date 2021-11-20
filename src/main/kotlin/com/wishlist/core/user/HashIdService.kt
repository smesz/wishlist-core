package com.wishlist.core.user

import org.hashids.Hashids
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service

@Service
class HashIdService(
    @Value("\${hashes.salt}") private val salt: String,
    @Value("\${hashes.minLength}") private val minHashLength: Int
) {

    private val hashIds = Hashids(salt, minHashLength)

    fun generateShortHash(value: Long): String {
        return hashIds.encode(value)
    }
}