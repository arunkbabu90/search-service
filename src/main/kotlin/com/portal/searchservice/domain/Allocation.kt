package com.portal.searchservice.domain

import jakarta.persistence.*
import java.time.Instant

@Entity
@Table(name = "allocations")
data class Allocation(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long,

    @Column(name = "project_name", nullable = false)
    val projectName: String,

    @Column(name = "type", nullable = false)
    val type: String,

    @Column(name = "start_date", nullable = false)
    val startDate: Instant,

    @Column(name = "end_date", nullable = false)
    val endDate: Instant,

    @OneToMany(mappedBy = "allocation", fetch = FetchType.LAZY, cascade = [CascadeType.ALL])
    val timesheets: List<Timesheet>
)
