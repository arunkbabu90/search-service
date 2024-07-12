package com.portal.searchservice.domain

import jakarta.persistence.*
import java.io.Serializable

@Entity
@Table(name = "users")
data class User(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long = -1,

    @Column(unique = true, nullable = false)
    var username: String,

    @Column(name = "full_name", nullable = false)
    var fullname: String,

    @OneToMany(mappedBy = "user", fetch = FetchType.LAZY, cascade = [CascadeType.ALL])
    var timesheet: List<Timesheet>? = null
) : Serializable
