package com.portal.searchservice.domain

import jakarta.persistence.*
import java.io.Serializable
import java.time.Instant

@Entity
@Table(name = "timesheet")
data class Timesheet(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long = 0,

    @Column(nullable = false)
    var project: String? = null,

    @Column(nullable = false)
    var task: String? = null,

    @Column(nullable = false)
    var hours: Int = 0,

    @Column(nullable = false)
    var description: String? = null,

    @Column(name = "timesheet_date", nullable = false)
    var timesheetDate: Instant,

    @Column(name = "updated_at")
    var updatedAt: Instant? = Instant.now(),

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    var user: User?
) : Serializable
