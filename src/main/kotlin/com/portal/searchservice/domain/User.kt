package com.portal.searchservice.domain

import jakarta.persistence.*
import java.io.Serializable

@Entity
@Table(name = "users")
data class User(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @Column(unique = true, nullable = false)
    val username: String,

    @Column(name = "full_name", nullable = false)
    val fullname: String
) : Serializable
