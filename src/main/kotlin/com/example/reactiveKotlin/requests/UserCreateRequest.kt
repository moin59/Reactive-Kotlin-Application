package com.example.reactiveKotlin.requests

import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotEmpty

data class UserCreateRequest(
    @field:NotEmpty
    var firstName: String,

    @field:NotEmpty
    var lastName: String,

    @field:Email
    var email: String
)
