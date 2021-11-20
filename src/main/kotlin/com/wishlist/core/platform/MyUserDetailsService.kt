package com.wishlist.core.platform

import com.wishlist.core.user.UserRepository
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.userdetails.ReactiveUserDetailsService
import org.springframework.security.core.userdetails.User
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono

@Component
class MyUserDetailsService(
    private val userRepository: UserRepository
) : ReactiveUserDetailsService {

    override fun findByUsername(email: String): Mono<UserDetails> {
        return userRepository.findByEmail(email)
            .map { userEntity ->
                User(
                    userEntity.email,
                    userEntity.password,
                    extractAuthorities(userEntity.roles)
                )
            }
    }

    private fun extractAuthorities(roles: Set<String>): Collection<GrantedAuthority> {
        return roles.map { SimpleGrantedAuthority(it) }
    }
}