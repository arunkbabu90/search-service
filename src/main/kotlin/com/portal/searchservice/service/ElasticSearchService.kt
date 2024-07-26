package com.portal.searchservice.service

import co.elastic.clients.elasticsearch._types.FieldValue
import co.elastic.clients.elasticsearch._types.query_dsl.BoolQuery
import co.elastic.clients.elasticsearch._types.query_dsl.QueryBuilders
import com.portal.searchservice.domain.Configuration
import com.portal.searchservice.domain.TimesheetDocument
import com.portal.searchservice.dto.Filter
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.data.elasticsearch.client.elc.NativeQuery
import org.springframework.data.elasticsearch.core.ElasticsearchOperations
import org.springframework.data.elasticsearch.core.query.Criteria
import org.springframework.data.elasticsearch.core.query.CriteriaQuery
import org.springframework.data.elasticsearch.core.query.FetchSourceFilter
import org.springframework.data.elasticsearch.core.query.SearchTemplateQuery
import org.springframework.data.elasticsearch.core.script.Script
import org.springframework.stereotype.Service
import java.time.Instant


@Service
class ElasticSearchServiceImpl(private val operations: ElasticsearchOperations) : ElasticSearchService {

    override fun getTimesheetWithConfiguration(
        userId: Long,
        configuration: Configuration
    ): List<TimesheetDocument> {
        val (limit, sorts, filterGroups) = configuration

        val boolQuery: BoolQuery.Builder = buildBoolQuery(filterGroups)
        val sortOptions = buildSortOptions(sorts)
        val pageable = PageRequest.of(0, limit)

        // Build the sort options
        val nativeQuery = NativeQuery.builder()
            .withQuery { q -> q.bool(boolQuery.build()) }
            .withPageable(pageable)
            .build()

        return operations.search(nativeQuery, TimesheetDocument::class.java).mapNotNull { it.content }
    }

    override fun getTimesheetBetweenDatesFilterByFields(
        userId: Long,
        startDate: Instant,
        endDate: Instant,
        vararg fields: String
    ): List<TimesheetDocument> {
        val criteria = Criteria("timesheet_date")
            .between(startDate, endDate)
            .and("user_id").`is`(userId)

        val query = CriteriaQuery(criteria).apply {
            addSourceFilter(FetchSourceFilter(fields, arrayOf()))
            sort = Sort.by(Sort.Order.asc("timesheet_date"), Sort.Order.asc("hours"))
        }

        return operations.search(query, TimesheetDocument::class.java).mapNotNull { it.content }
    }

    override fun getTimesheetOnScript(scriptId: String, fields: Map<String, String>): List<TimesheetDocument> {
        val query = SearchTemplateQuery.builder()
            .withId(scriptId)
            .withParams(fields)
            .build()

        return operations.search(query, TimesheetDocument::class.java).mapNotNull { it.content }
    }

    override fun createStoredScript(scriptId: String, script: String): Boolean {
        val esScript = Script.builder().apply {
            withId(scriptId)
            withLanguage("mustache")
            withSource(script)
        }.build()

        return operations.putScript(esScript)
    }

    private fun buildSortOptions(sorts: List<com.portal.searchservice.dto.Sort>): List<Sort.Order> {
        return sorts.map { sort ->
            when (sort.direction.lowercase()) {
                "asc", "ascending" -> {
                    Sort.Order.asc(sort.field)
                }
                else -> Sort.Order.desc(sort.field)
            }
        }
    }

    private fun buildBoolQuery(filters: List<Filter>): BoolQuery.Builder {
        val boolQuery = QueryBuilders.bool()

        filters.forEach { filter ->
            if (filter.values.isNotEmpty()) {
                boolQuery.must {
                    it.terms { t ->
                        t.field(filter.field).terms { t1 ->
                            t1.value(filter.values.map { v -> FieldValue.of(v) })
                        }
                    }
                }
            } else if (filter.value.isNotEmpty()) {
                boolQuery.must {
                    it.match { m -> m.field(filter.field).query(filter.value) }
                }
            }
        }

        return boolQuery
    }
}

interface ElasticSearchService {
    fun getTimesheetWithConfiguration(userId: Long,
                                      configuration: Configuration) = listOf<TimesheetDocument>()

    fun getTimesheetBetweenDatesFilterByFields(userId: Long,
                                               startDate: Instant,
                                               endDate: Instant,
                                               vararg fields: String) = listOf<TimesheetDocument>()

    fun getTimesheetOnScript(scriptId: String, fields: Map<String, String>) = listOf<TimesheetDocument>()
    fun createStoredScript(scriptId: String, script: String): Boolean = false
}