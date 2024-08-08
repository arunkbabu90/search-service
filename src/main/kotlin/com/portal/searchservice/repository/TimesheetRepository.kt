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

        val filterQuery = buildFilterQuery(configuration.filters)
        val sortOrderQuery = buildSortQuery(configuration.sorts)
        val limitQuery = buildLimitQuery(configuration.limit)

        queryJoiner.add(filterQuery)
        queryJoiner.add(sortOrderQuery)
        queryJoiner.add(limitQuery)

        println("$queryJoiner")

        return entityManager.createNativeQuery(queryJoiner.toString(), Map::class.java)
            .resultList as List<Map<String, Any>>
    }

    private fun buildLimitQuery(limit: Int) = if (limit > 0) "LIMIT $limit" else "LIMIT $DEFAULT_RESULT_LIMIT"

    private fun buildFilterQuery(filters: List<Filter>): String {
        val mainJoiner = StringJoiner(" ")
        val filterJoiner = StringJoiner(" AND ")

        filters.forEach { filter: Filter ->
            // Value(s) only

            if (filter.values.isNotEmpty()) {
                when (filter.operator) {
                    IN -> {
                        // TODO: VERIFIED
                        val inValueJoiner = StringJoiner(", ")
                        filter.values.forEach { inValueJoiner.add("""'$it'""") }

                        filterJoiner.add("${filter.field} IN (${inValueJoiner})")
                    }

                    NOT_IN -> {
                        // TODO: VERIFIED
                        val inValueJoiner = StringJoiner(", ")
                        filter.values.forEach { inValueJoiner.add("""'$it'""") }

                        filterJoiner.add("${filter.field} NOT IN (${inValueJoiner})")
                    }

                    else -> throw BadRequestException("Invalid Request. Please check the configuration and try again")
                }
            } else if (filter.value.isNotBlank()) {
                // Value only
                var value = if (filter.value.isNumber() || filter.value.isBoolean()) {
                    filter.value
                } else {
                    """'${filter.value}'"""
                }

                when (filter.operator) {
                    EQUAL_TO -> {
                        // TODO: VERIFIED
                        filterJoiner.add("${filter.field} = $value")
                    }

                    NOT_EQUAL_TO -> {
                        // TODO: VERIFIED
                        filterJoiner.add("${filter.field} != $value")
                    }

                    BETWEEN -> {
                        // Range Query. Both upper and lower bounds are Inclusive
                        if (filter.highValue.isNotBlank()) {
                            val highValue = if (filter.highValue.isNumber() || filter.highValue.isBoolean()) {
                                filter.highValue
                            } else {
                                """'${filter.highValue}'"""
                            }

                            filterJoiner.add("${filter.field} BETWEEN $value AND $highValue")
                        } else {
                            throw BadRequestException("highValue is required")
                        }
                    }

                    GREATER_THAN -> {
                        filterJoiner.add("${filter.field} > $value")
                    }

                    GREATER_THAN_EQUAL_TO -> {
                        filterJoiner.add("${filter.field} >= $value")
                    }

                    LESS_THAN -> {
                        filterJoiner.add("${filter.field} < $value")
                    }

                    LESS_THAN_EQUAL_TO -> {
                        filterJoiner.add("${filter.field} <= $value")
                    }

                    CONTAINS -> {
                        // TODO: VERIFIED
                        value = if (filter.value.isNumber() || filter.value.isBoolean()) {
                            filter.value
                        } else {
                            val words = filter.value.split(" ")
                            val commaSeparatedWords = words.joinToString(separator = ", ") { word -> """'%$word%'""" }
                            commaSeparatedWords
                        }

                        filterJoiner.add("${filter.field} ILIKE ANY(ARRAY[$value])")
                    }

                    NOT_CONTAINS -> {
                        // TODO: VERIFIED
                        val conditionals = if (filter.value.isNumber() || filter.value.isBoolean()) {
                            """${filter.field} ILIKE '%${filter.value}%'"""
                        } else {
                            val words = filter.value.split(" ")
                            words.joinToString(separator = " OR ") { word ->
                                """${filter.field} ILIKE '%$word%'"""
                            }
                        }
                        filterJoiner.add("NOT ($conditionals)")
                    }

                    else -> throw BadRequestException("Invalid Request. Please check the configuration and try again")
                }

            } else {
                throw BadRequestException("Bad Request!")
            }
        }

        mainJoiner.add("WHERE")
        mainJoiner.add(filterJoiner.toString())

        return mainJoiner.toString()
    }


    private fun buildSortQuery(sorts: List<Sort>): String {
        val mainJoiner = StringJoiner(" ")
        val sortJoiner = StringJoiner(", ")
        sorts.forEach { sort ->
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
            mainJoiner.add("ORDER BY")
            mainJoiner.add(sortJoiner.toString())
        }

        return mainJoiner.toString()
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
