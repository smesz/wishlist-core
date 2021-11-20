package com.wishlist.core.user

import com.wishlist.core.platform.logger
import com.wishlist.core.user.api.CreateUserRequest
import com.wishlist.core.user.exception.UserAlreadyRegisteredException
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono
import java.time.Instant

@Service
class UserService(
    private val userRepository: UserRepository,
    private val hashIdService: HashIdService,
    private val passwordEncoder: PasswordEncoder
) {

    private val log by logger()

    fun createUser(createUserRequest: CreateUserRequest): Mono<UserEntity> {
        return userRepository.findByEmail(createUserRequest.email)
            .flatMap {
                log.warn("User ${createUserRequest.email} already registered")
                Mono.error<UserEntity>(UserAlreadyRegisteredException(createUserRequest.email))
            }
            .switchIfEmpty(
                userRepository.count().flatMap { userCount ->
                    // register new user
                    val now = Instant.now()

                    val entity = UserEntity(
                        email = createUserRequest.email,
                        password = passwordEncoder.encode(createUserRequest.password),
                        firstName = createUserRequest.firstName,
                        lastName = createUserRequest.lastName,
                        birthday = createUserRequest.birthday,
                        registeredAt = now,
                        updatedAt = now,
                        roles = mutableSetOf("BASIC_USER"),
                        userHash = hashIdService.generateShortHash(userCount + 1)
                    )

                    userRepository.save(entity).doOnSuccess {
                        log.info("Registered new user: $it")
                    }
                }
            )
    }
}