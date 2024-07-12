package com.portal.searchservice.service

import com.portal.searchservice.domain.User
import com.portal.searchservice.exception.ResourceNotFoundException
import com.portal.searchservice.repository.UserRepository
import org.springframework.stereotype.Service

@Service
class UserServiceImpl(private val userRepository: UserRepository) : UserService {

    override fun getAllUsers(): List<User> = userRepository.findAll()

    override fun getUserDetails(username: String): User {
        return userRepository.findByUsername(username)
            ?: throw ResourceNotFoundException("User $username not found")
    }

    override fun addUser(user: User): User = userRepository.save(user)
}

interface UserService {
    fun getAllUsers(): List<User> = listOf()
    fun getUserDetails(username: String) = User(0, "", "", )
    fun addUser(user: User): User = User(0, "", "")
}