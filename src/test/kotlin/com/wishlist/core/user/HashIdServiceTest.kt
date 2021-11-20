package com.wishlist.core.user

import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.string.shouldHaveMinLength

class HashIdServiceTest : ShouldSpec({

    val service = HashIdService("salt", 6)

    should("create hash with minimum length of 6 characters") {
        service.generateShortHash(1) shouldHaveMinLength 6
    }
})
