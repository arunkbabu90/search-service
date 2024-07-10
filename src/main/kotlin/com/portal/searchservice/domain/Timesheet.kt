package com.portal.searchservice.domain

import jakarta.persistence.*
import java.io.Serializable
import java.time.Instant

@Entity
@Table(name = "timesheet")
data class Timesheet(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @Column(nullable = false)
    val project: String? = null,

    @Column(nullable = false)
    val task: String? = null,

    @Column(nullable = false)
    val hours: Int = 8,

    @Column(nullable = false)
    val description: String? = null,

    @Column(name = "timesheet_date", nullable = false)
    val timesheetDate: Instant,

    @Column(name = "updated_at")
    val updatedAt: Instant = Instant.now(),

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    val user: User
) : Serializable
