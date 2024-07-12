package com.portal.searchservice.controller

import com.portal.searchservice.dto.UserDto
import com.portal.searchservice.mapper.UserMapper
import com.portal.searchservice.service.UserService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/user")
class UserControllerImpl(
    private val userService: UserService,
    private val mapper: UserMapper
) : UserController<UserDto> {

    @GetMapping("/all")
    override fun getAllUsers(): ResponseEntity<List<UserDto>> {
        val usersList = userService.getAllUsers().map { mapper.toUserDto(it) }
        return ResponseEntity(usersList, HttpStatus.OK)
    }

    @GetMapping
    override fun getUserDetails(@RequestParam("u") username: String): ResponseEntity<UserDto> {
        val user = userService.getUserDetails(username)
        val userDto = mapper.toUserDto(user)
        return ResponseEntity(userDto, HttpStatus.OK)
    }

    @PostMapping
    override fun saveUser(@RequestBody body: UserDto): ResponseEntity<UserDto> {
        val user = mapper.toUser(body)
        val savedUser = userService.addUser(user)
        val savedUserDto = mapper.toUserDto(savedUser)
        return ResponseEntity(savedUserDto, HttpStatus.OK)
    }
}

interface UserController<T> {
    fun getAllUsers(): ResponseEntity<List<T>> = ResponseEntity(HttpStatus.NOT_IMPLEMENTED)
    fun getUserDetails(username: String): ResponseEntity<T> = ResponseEntity(HttpStatus.NOT_IMPLEMENTED)
    fun saveUser(body: T): ResponseEntity<T> = ResponseEntity(HttpStatus.NOT_IMPLEMENTED)
}