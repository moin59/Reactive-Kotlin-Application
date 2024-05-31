package com.example.reactiveKotlin.responses

data class UserUpdateResponse (

    val id: Int?,
    val firstName: String,
    val lastName: String,
    val email: String
)