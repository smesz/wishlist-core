package com.wishlist.core.user

import com.wishlist.core.test.TestObjects.anUserEntity
import com.wishlist.core.user.api.CreateUserRequest
import com.wishlist.core.user.exception.UserAlreadyRegisteredException
import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.should
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import org.springframework.security.crypto.password.PasswordEncoder
import reactor.core.publisher.Mono
import reactor.kotlin.test.test
import java.time.LocalDate

class UserServiceTest : ShouldSpec({

    val userRepository = mockk<UserRepository>()
    val hashService = mockk<HashIdService>()
    val passwordEncoder = mockk<PasswordEncoder>()
    val service = UserService(userRepository, hashService, passwordEncoder)

    should("create new user with success") {
        // given
        val request = CreateUserRequest(
            firstName = "Adam",
            lastName = "Smith",
            email = "adam.smith@gmail.com",
            password = "secret password",
            birthday = LocalDate.of(1991, 10, 3)
        )

        // mocks
        every { userRepository.findByEmail("adam.smith@gmail.com") } returns Mono.empty()
        every { userRepository.count() } returns Mono.just(0)
        every { passwordEncoder.encode("secret password") } returns "bcrypt password"
        every { hashService.generateShortHash(1) } returns "hash"

        val userEntitySlot = slot<UserEntity>()
        every { userRepository.save(capture(userEntitySlot)) } answers { Mono.just(userEntitySlot.captured) }

        // when
        val userEntityMono = service.createUser(request)

        // then
        userEntityMono.test()
            .expectNextCount(1)
            .verifyComplete()

        // and - assert created entity
        userEntitySlot.captured should {
            it.firstName shouldBe "Adam"
            it.lastName shouldBe "Smith"
            it.password shouldBe "bcrypt password"
            it.userHash shouldBe "hash"
            it.roles shouldContainExactly setOf("BASIC_USER")
        }
    }

    should("create not create new user if user already exists") {
        // given
        val request = CreateUserRequest(
            firstName = "Adam",
            lastName = "Smith",
            email = "adam.smith@gmail.com",
            password = "secret password",
            birthday = LocalDate.of(1991, 10, 3)
        )

        // mocks
        every { userRepository.findByEmail("adam.smith@gmail.com") } returns Mono.just(anUserEntity())

        // when
        val userEntityMono = service.createUser(request)

        // then
        userEntityMono.test()
            .expectError(UserAlreadyRegisteredException::class.java)
    }
})