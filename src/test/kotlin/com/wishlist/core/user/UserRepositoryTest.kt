package com.wishlist.core.user

import com.wishlist.core.test.BaseRepositoryTest
import com.wishlist.core.test.TestObjects.anUserEntity
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import reactor.kotlin.test.test

class UserRepositoryTest(@Autowired private val userRepository: UserRepository) : BaseRepositoryTest() {

    @AfterEach
    fun tearDown() {
        userRepository.deleteAll().block()
    }

    @Test
    @DisplayName("should find user by email")
    fun shouldFindUserByEmail() {
        userRepository.saveAll(
            listOf(
                anUserEntity().copy(email = "adam.smith@gmail.com", firstName = "Adam", lastName = "Smith"),
                anUserEntity().copy(email = "john.wick@gmail.com", firstName = "John", lastName = "Wick"),
                anUserEntity().copy(email = "robert.person@gmail.com", firstName = "Robert", lastName = "Person")
            )
        ).then(
            userRepository.findByEmail("john.wick@gmail.com")
        ).test()
            .assertNext { user ->
                user.email shouldBe "john.wick@gmail.com"
                user.firstName shouldBe "John"
                user.lastName shouldBe "Wick"
            }
            .verifyComplete()
    }
}