package com.portal.searchservice.repository

import com.portal.searchservice.domain.Configuration
import com.portal.searchservice.domain.Timesheet
import com.portal.searchservice.domain.User
import com.portal.searchservice.dto.Filter
import com.portal.searchservice.dto.Sort
import com.portal.searchservice.exception.BadRequestException
import jakarta.persistence.EntityManager
import jakarta.persistence.PersistenceContext
import jakarta.persistence.criteria.Path
import jakarta.persistence.criteria.Predicate
import org.springframework.data.jpa.domain.Specification
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.JpaSpecificationExecutor
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository
import utils.*
import java.util.*

interface TimesheetRepository : JpaRepository<Timesheet, Long>{
    fun findByUser(user: User): List<Timesheet>

    @Query("SELECT * FROM get_agg_timesheet() LIMIT 100", nativeQuery = true)
    fun findFirst100TimesheetsUsingFunction(): List<Map<String, Any>>

    @Query("SELECT * FROM get_agg_timesheet_by_username(:username) ORDER BY timesheet_id ASC LIMIT 1000", nativeQuery = true)
    fun findTimesheetsByUsernameUsingFunction(username: String): List<Map<String, Any>>
}

interface TimesheetRepository1 : JpaRepository<ArrayList<Map<String, Any>>, Long>, JpaSpecificationExecutor<ArrayList<Map<String, Any>>>

@Repository
class CustomTimesheetRepository(private val repository: TimesheetRepository1) {
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

//        val filterQuery = buildFiterQuery(configuration.filters)
//        val sortOrderQuery = buildSortQuery(configuration.sorts)
        val limitQuery = buildLimitQuery(configuration.limit)

        val spec = buildQuery(configuration.filters, configuration.sorts)

//        queryJoiner.add(filterQuery)
//        queryJoiner.add(sortOrderQuery)
        queryJoiner.add(limitQuery)

//        val a = entityManager.createNativeQuery(queryJoiner.toString(), Map::class.java)
//            .resultList as List<Map<String, Any>>

//        return a

//        val timesheets = repository.findAll(spec)
//        return timesheets.flatten()
        return emptyList()
    }

    private fun buildLimitQuery(limit: Int) = if (limit > 0) "LIMIT $limit" else "LIMIT $DEFAULT_RESULT_LIMIT"

    private fun buildFilterQuery(filters: List<Filter>): String {
        return ""
    }

    private fun buildSortQuery(sorts: List<Sort>): String {
        return ""
    }

    private fun buildQuery(filters: List<Filter>, sorts: List<Sort>): Specification<Any> {
        return Specification { root, query, criteriaBuilder ->
            val predicates = mutableListOf<Predicate>()

            filters.forEach { filter: Filter ->
                if (filter.values.isNotEmpty()) {
                    when (filter.operator) {
                        IN -> {
                            val path: Path<Any> = root.get(filter.field)
                            val clause = criteriaBuilder.`in`(path)
                            filter.values.forEach { clause.value(it) }

                            predicates.add(clause)
                        }

                        NOT_IN -> {
                            val path: Path<Any> = root.get(filter.field)
                            val clause = criteriaBuilder.`in`(path)
                            filter.values.forEach { clause.value(it) }

                            predicates.add(criteriaBuilder.not(clause))
                        }

                        else -> throw BadRequestException("Invalid Request. Please check the configuration and try again")
                    }
                } else if (filter.value.isNotBlank()) {
                    // Values only
                    when (filter.operator) {
                        EQUAL_TO -> {
                            val path = root.get<String>(filter.field)
                            val clause = criteriaBuilder.equal(path, filter.value)
                            predicates.add(clause)
                        }

                        NOT_EQUAL_TO -> {
                            val path = root.get<String>(filter.field)
                            val clause = criteriaBuilder.notEqual(path, filter.value)
                            predicates.add(clause)
                        }

                        BETWEEN -> {
                            // Range Query. Both upper and lower bounds are Inclusive
                            if (filter.highValue.isNotBlank()) {
                                val path: Path<String> = root.get(filter.field)
                                val clause = criteriaBuilder.between(path, filter.value, filter.highValue)

                                predicates.add(clause)
                            } else {
                                throw BadRequestException("highValue is required")
                            }
                        }

                        GREATER_THAN -> {
                            val path: Path<String> = root.get(filter.field)
                            val clause = criteriaBuilder.greaterThan(path, filter.value)

                            predicates.add(clause)
                        }

                        GREATER_THAN_EQUAL_TO -> {
                            val path: Path<String> = root.get(filter.field)
                            val clause = criteriaBuilder.greaterThanOrEqualTo(path, filter.value)

                            predicates.add(clause)
                        }

                        LESS_THAN -> {
                            val path: Path<String> = root.get(filter.field)
                            val clause = criteriaBuilder.lessThan(path, filter.value)

                            predicates.add(clause)
                        }

                        LESS_THAN_EQUAL_TO -> {
                            val path: Path<String> = root.get(filter.field)
                            val clause = criteriaBuilder.lessThanOrEqualTo(path, filter.value)

                            predicates.add(clause)
                        }

                        CONTAINS -> {
                            val path: Path<String> = root.get(filter.field)
                            val clause = criteriaBuilder.like(path, "%${filter.value}%")

                            predicates.add(clause)
                        }

                        NOT_CONTAINS -> {
                            val path: Path<String> = root.get(filter.field)
                            val clause = criteriaBuilder.notLike(path, "%${filter.value}%")

                            predicates.add(clause)
                        }

                        else -> throw BadRequestException("Invalid Request. Please check the configuration and try again")
                    }

                } else {
                    throw BadRequestException("Bad Request!")
                }
            }

            val sortOrders = sorts.map { sort ->
                val (field, direction) = sort
                val path = root.get<Any>(field)
                when (direction.lowercase()) {
                    "asc", "ascending" -> {
                        criteriaBuilder.asc(path)
                    }
                    "desc", "descending" -> {
                        criteriaBuilder.desc(path)
                    }
                    else -> throw IllegalArgumentException("Invalid Sort Direction ${sort.direction}")
                }
            }

            query.orderBy(sortOrders)
            criteriaBuilder.and(*predicates.toTypedArray())
        }
    }

}
