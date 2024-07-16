package com.portal.searchservice.service

import com.portal.searchservice.domain.Timesheet
import com.portal.searchservice.domain.TimesheetDocument
import com.portal.searchservice.exception.ResourceNotFoundException
import com.portal.searchservice.repository.TimesheetRepository
import com.portal.searchservice.repository.UserRepository
import org.springframework.data.domain.PageRequest
import org.springframework.data.elasticsearch.core.ElasticsearchOperations
import org.springframework.data.elasticsearch.core.query.Criteria
import org.springframework.data.elasticsearch.core.query.CriteriaQuery
import org.springframework.stereotype.Service
import java.time.Instant

@Service
class TimesheetServiceImpl(
    private val timesheetRepository: TimesheetRepository,
    private val userRepository: UserRepository,
    private val operations: ElasticsearchOperations
) : TimesheetService<Timesheet, TimesheetDocument> {

    override fun findTimesheetsBetween(
        username: String,
        startDate: Instant,
        endDate: Instant,
        from: Int,
        size: Int
    ): List<TimesheetDocument> {
        val user = userRepository.findByUsername(username)
            ?: throw ResourceNotFoundException("User $username not found")

        val userId: Long = user.id
        val pageable = PageRequest.of(from, size)

        val criteria = Criteria("timesheet_date").between(startDate, endDate)
            .and("user_id").`is`(userId)
        val query = CriteriaQuery(criteria, pageable)

        return operations.search(query, TimesheetDocument::class.java).mapNotNull { it.content }
    }

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

interface TimesheetService<T, out TD> {
    fun findTimesheetsWithConfig(searchScript: String) = listOf<TD>()
    fun findTimesheetsBetween(username: String, startDate: Instant, endDate: Instant, from: Int, size: Int) = listOf<TD>()
    fun getTimesheets(username: String): List<T> = listOf()
    fun saveTimesheetEntry(username: String, timesheet: T): T
}
