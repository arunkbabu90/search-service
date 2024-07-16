package com.portal.searchservice.repository

import com.portal.searchservice.domain.Timesheet
import com.portal.searchservice.domain.User
import org.springframework.data.jpa.repository.JpaRepository

interface TimesheetRepository : JpaRepository<Timesheet, Long> {
    fun findByUser(user: User): List<Timesheet>
}
