package com.portal.searchservice.service

import com.portal.searchservice.domain.Timesheet
import com.portal.searchservice.exception.ResourceNotFoundException
import com.portal.searchservice.repository.TimesheetRepository
import com.portal.searchservice.repository.UserRepository
import org.springframework.stereotype.Service
import java.time.Instant

@Service
class TimesheetServiceImpl(
    private val timesheetRepository: TimesheetRepository,
    private val userRepository: UserRepository
) : TimesheetService<Timesheet> {

    override fun getTimesheets(username: String): List<Timesheet> {
        val user = userRepository.findByUsername(username)
            ?: throw ResourceNotFoundException("User $username not found")

        return timesheetRepository.findByUser(user)
    }

    override fun saveTimesheetEntry(username: String, timesheet: Timesheet): Timesheet {
        val user = userRepository.findByUsername(username)
            ?: throw ResourceNotFoundException("User $username not found")

        timesheet.apply {
            this.user = user
            this.updatedAt = Instant.now()
        }

        return timesheetRepository.save(timesheet)
    }
}

interface TimesheetService<T> {
    fun getTimesheets(username: String): List<T> = listOf()
    fun saveTimesheetEntry(username: String, timesheet: T): T
}
