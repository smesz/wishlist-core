package com.wishlist.core.platform

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpMethod
import org.springframework.security.authentication.ReactiveAuthenticationManager
import org.springframework.security.authentication.UserDetailsRepositoryReactiveAuthenticationManager
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity
import org.springframework.security.config.web.server.ServerHttpSecurity
import org.springframework.security.config.web.server.invoke
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.web.server.SecurityWebFilterChain
import org.springframework.security.web.server.util.matcher.PathPatternParserServerWebExchangeMatcher

@Configuration
@EnableWebFluxSecurity
class WebSecurityConfiguration(
    private val userDetailsService: MyUserDetailsService
) {

    @Bean
    fun springSecurityFilterChain(
        http: ServerHttpSecurity,
        reactiveAuthenticationManager: ReactiveAuthenticationManager
    ): SecurityWebFilterChain? {
        return http {
            csrf { disable() }
            httpBasic {
                authenticationManager = reactiveAuthenticationManager
            }
            formLogin { disable() }
            authorizeExchange {
                authorize("/actuator/**", permitAll)
                authorize(
                    PathPatternParserServerWebExchangeMatcher("/api/v1/user", HttpMethod.POST),
                    permitAll
                )
                authorize("/api/**", hasAuthority("BASIC_USER"))
                authorize("/**", denyAll)
            }
        }
    }

    @Bean
    fun reactiveAuthenticationManager(passwordEncoder: PasswordEncoder): ReactiveAuthenticationManager {
        return UserDetailsRepositoryReactiveAuthenticationManager(
            userDetailsService
        ).apply {
            this.setPasswordEncoder(passwordEncoder)
        }
    }

    @Bean
    fun passwordEncoder(): PasswordEncoder {
        return BCryptPasswordEncoder()
    }
}