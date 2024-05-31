package com.example.reactiveKotlin.repositories

import com.example.reactiveKotlin.models.User
import org.springframework.data.r2dbc.repository.Query
import org.springframework.data.r2dbc.repository.R2dbcRepository
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

interface UserRepository: R2dbcRepository<User, Int> {

    fun findByEmail(email: String): Mono<User>

    @Query("SELECT * FROM users limit :limit offset :offset")
    fun findAllUsers(limit: Int, offset: Int): Flux<User>
}