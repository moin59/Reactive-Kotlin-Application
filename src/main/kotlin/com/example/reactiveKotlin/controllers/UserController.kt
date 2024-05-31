package com.example.reactiveKotlin.controllers

import com.example.reactiveKotlin.models.User
import com.example.reactiveKotlin.repositories.UserRepository
import com.example.reactiveKotlin.requests.UserCreateRequest
import com.example.reactiveKotlin.requests.UserUpdateRequest
import com.example.reactiveKotlin.responses.PagingResponse
import com.example.reactiveKotlin.responses.UserUpdateResponse
import com.johncooper.reactiveKotlin.responses.UserCreateResponse
import jakarta.validation.Valid
import kotlinx.coroutines.reactive.awaitFirst
import kotlinx.coroutines.reactive.awaitFirstOrElse
import kotlinx.coroutines.reactive.awaitFirstOrNull
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.*
import org.springframework.web.server.ResponseStatusException

@RestController
@RequestMapping("/users", produces = [MediaType.APPLICATION_JSON_VALUE])
class UserController {

    @Autowired
    lateinit var userRepository: UserRepository

    @PostMapping("")
    protected suspend fun createUser(
        @RequestBody @Valid request: UserCreateRequest
    ): UserCreateResponse {
       val existingUser = userRepository.findByEmail(request.email).awaitFirstOrNull()

        if (existingUser != null){
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Duplicate user")
        }

        val user = User(
            id = null,
            email = request.email,
            firstName = request.firstName,
            lastName = request.lastName
        )

       val createUser = try{
            userRepository.save(user).awaitFirstOrNull()
        }catch (e: Exception){
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Unable to create user", e)
        }

        val id = createUser?.id ?:
        throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Missing id for created user")

        return UserCreateResponse(
            id = id,
            email = createUser.email,
            firstName = createUser.firstName,
            lastName = createUser.lastName
        )
    }

    @GetMapping("")
    suspend fun listUsers(
        @RequestParam pageNo:Int =1,
        @RequestParam  pageSize: Int = 10
    ): PagingResponse<User> {
        var limit = pageSize
        var offset = (limit * pageNo) - limit

        val list = userRepository.findAllUsers(limit, offset).collectList().awaitFirst()
        val total = userRepository.count().awaitFirst()

        return PagingResponse(total, list)

    }

    @PatchMapping("/{userId}")
    suspend fun updateUser(
        @PathVariable userId: Int,
        @RequestBody @Valid userUpdateRequest: UserUpdateRequest
    ): UserUpdateResponse {

        var existingDBUser = userRepository.findById(userId).awaitFirstOrElse {
            throw ResponseStatusException(HttpStatus.NOT_FOUND, "user #$userId does not exist")
        }

        val duplicateUser = userRepository.findByEmail(userUpdateRequest.email.toString()).awaitFirstOrNull()
        if (duplicateUser != null && duplicateUser.id != userId) {
            throw ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "Duplicate user: user with email ${userUpdateRequest.email} already exists"
            )
        }

        val updatedUser =  try {

        existingDBUser.email = userUpdateRequest.email.toString()
        existingDBUser.firstName = userUpdateRequest.firstName ?: existingDBUser.firstName
        existingDBUser.lastName = userUpdateRequest.lastName ?: existingDBUser.lastName

        userRepository.save(existingDBUser).awaitFirst()
        }catch (e: Exception){
            throw ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Unable to update user", e)
        }

        return UserUpdateResponse(
            updatedUser.id,
            updatedUser.email,
            updatedUser.firstName,
            updatedUser.lastName)
    }

}