package com.portal.searchservice.repository

import com.portal.searchservice.domain.Configuration
import com.portal.searchservice.domain.Timesheet
import com.portal.searchservice.domain.User
import com.portal.searchservice.dto.Filter
import com.portal.searchservice.exception.BadRequestException
import jakarta.persistence.EntityManager
import jakarta.persistence.PersistenceContext
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository
import utils.*
import java.util.*

interface TimesheetRepository : JpaRepository<Timesheet, Long> {
    fun findByUser(user: User): List<Timesheet>

    @Query("SELECT * FROM get_agg_timesheet() LIMIT 100", nativeQuery = true)
    fun findFirst100TimesheetsUsingFunction(): List<Map<String, Any>>

    @Query("SELECT * FROM get_agg_timesheet_by_username(:username) ORDER BY timesheet_id ASC LIMIT 1000", nativeQuery = true)
    fun findTimesheetsByUsernameUsingFunction(username: String): List<Map<String, Any>>
}

@Repository
class CustomTimesheetRepository {
    @PersistenceContext
    private lateinit var entityManager: EntityManager

    fun findTimesheetsByUsernameUsingFunction(username: String): List<Map<String, Any>> {
        val query = "SELECT * FROM get_agg_timesheet_by_username(:username) ORDER BY timesheet_id ASC LIMIT 100"
        val result = entityManager.createNativeQuery(query, Map::class.java).apply {
            setParameter("username", username)
        }.resultList as List<Map<String, Any>>
        return result
    }

    fun findTimesheetByConfiguration(configuration: Configuration): List<Map<String, Any>> {
        val queryJoiner = StringJoiner(" ")
        queryJoiner.add("SELECT * FROM get_agg_timesheet()")

        configuration.filters.forEach { filter: Filter ->

            if (filter.values.isNotEmpty()) {
                when (filter.operator) {
                    IN -> {

                    }

                    NOT_IN -> {

                    }

                    else -> throw BadRequestException("Invalid Request. Please check the configuration and try again")
                }
            } else if (filter.value.isNotBlank()) {
                // Values only
                when (filter.operator) {
                    EQUAL_TO -> {
                        if (filter.value.isString()) {

                        } else {

                        }
                    }

                    NOT_EQUAL_TO -> {
                        if (filter.value.isString()) {

                        } else {

                        }
                    }

                    BETWEEN -> {
                        // Range Query. Both upper and lower bounds are Inclusive
                        if (filter.highValue.isNotBlank()) {

                        } else {
                            throw BadRequestException("highValue is required")
                        }
                    }

                    GREATER_THAN -> {

                    }

                    GREATER_THAN_EQUAL_TO -> {

                    }

                    LESS_THAN -> {

                    }

                    LESS_THAN_EQUAL_TO -> {

                    }

                    CONTAINS -> {

                    }

                    NOT_CONTAINS -> {

                    }

                    else -> throw BadRequestException("Invalid Request. Please check the configuration and try again")
                }

            } else {
                throw BadRequestException("Bad Request!")
            }
        }

        val sortJoiner = StringJoiner(",")
        configuration.sorts.forEach { sort ->
            val (field, direction) = sort
            when (direction.lowercase()) {
                "asc", "ascending" -> {
                    sortJoiner.add("$field ASC")
                }
                "desc", "descending" -> {
                    sortJoiner.add("$field DESC")
                }
            }
        }

        if (sortJoiner.length() > 0) {
            queryJoiner.add("ORDER BY")
            queryJoiner.add(sortJoiner.toString())
        }

        val limit = configuration.limit
        queryJoiner.add(if (limit > 0) "LIMIT $limit" else "LIMIT $DEFAULT_RESULT_LIMIT")

        val a = entityManager.createNativeQuery(queryJoiner.toString(), Map::class.java)
            .resultList as List<Map<String, Any>>

        return a
    }

    private fun buildSort() {

    }
}
