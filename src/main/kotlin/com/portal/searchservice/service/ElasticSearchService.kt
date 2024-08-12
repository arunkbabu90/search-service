package com.portal.searchservice.service

import co.elastic.clients.elasticsearch.ElasticsearchClient
import co.elastic.clients.elasticsearch._types.FieldValue
import co.elastic.clients.elasticsearch._types.SortOptions
import co.elastic.clients.elasticsearch._types.mapping.Property
import co.elastic.clients.elasticsearch._types.query_dsl.BoolQuery
import co.elastic.clients.elasticsearch._types.query_dsl.QueryBuilders
import co.elastic.clients.elasticsearch.core.SearchResponse
import co.elastic.clients.elasticsearch.indices.GetMappingRequest
import co.elastic.clients.json.JsonData
import com.portal.searchservice.domain.Configuration
import com.portal.searchservice.domain.TimesheetDocument
import com.portal.searchservice.dto.Filter
import com.portal.searchservice.exception.BadRequestException
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.data.elasticsearch.core.ElasticsearchOperations
import org.springframework.data.elasticsearch.core.query.Criteria
import org.springframework.data.elasticsearch.core.query.CriteriaQuery
import org.springframework.data.elasticsearch.core.query.FetchSourceFilter
import org.springframework.data.elasticsearch.core.query.SearchTemplateQuery
import org.springframework.data.elasticsearch.core.script.Script
import org.springframework.stereotype.Service
import utils.*
import java.time.Instant


@Service
class ElasticSearchServiceImpl(
    private val operations: ElasticsearchOperations,
    private val client: ElasticsearchClient
) : ElasticSearchService {

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

    override fun getTimesheetWithConfiguration(
        configuration: Configuration
    ): List<Map<String, Any>> {
        val (limitValue, sorts, filters) = configuration
        val limit = if (limitValue > 0) limitValue else DEFAULT_RESULT_LIMIT

        val boolQuery: BoolQuery = buildBoolQuery(filters)
        val sortOptions = buildSortOptions(sorts)
        val pageable = PageRequest.of(0, limit)

        val searchResponse: SearchResponse<Map<*, *>> = client.search({ s ->
            s.index("hrms-timesheet-detailed")
                .query { it.bool(boolQuery) }
                .from(pageable.pageNumber * pageable.pageSize)
                .size(pageable.pageSize)
                .sort(sortOptions)
        }, Map::class.java)

        val hitsMeta = searchResponse.hits()
        val totalHits = hitsMeta.total()?.value() ?: 0

        return hitsMeta.hits().mapNotNull { it.source() as Map<String, Any> }
            .toMutableList().apply {
                add(mapOf("total_hits" to totalHits))
            }
    }

    private fun buildSortOptions(sorts: List<com.portal.searchservice.dto.Sort>): List<SortOptions> {
        return sorts.map { sort ->
            SortOptions.Builder().field { f ->
                val fieldName = if (isStringField(sort.field)) { "${sort.field}.keyword" } else { sort.field }
                f.field(fieldName).order(sort.direction.toSortOrder())
            }.build()
        }
    }

    private fun isStringField(field: String): Boolean {
        val index = "hrms-timesheet-detailed"
        val request = GetMappingRequest.Builder()
            .index(index)
            .build()

        val response = client.indices().getMapping(request)
        val mappings: Property? = response.result()[index]?.mappings()?.properties()?.get(field)

        return mappings?.isText ?: false
    }

    private fun buildBoolQuery(filters: List<Filter>): BoolQuery {
        val boolQuery = QueryBuilders.bool()

        // TODO: By default it's sorting by timesheet_id or id to be precise.
        //  And it shouldn't sort by ID by default unless a filter is specified
        filters.forEach { filter ->
            if (filter.values.isNotEmpty()) {
                when (filter.operator) {
                    IN -> {
                        boolQuery.must {
                            it.terms { t ->
                                val refValue = filter.values.firstOrNull()
                                val fieldName = if (refValue?.isString() == true) {
                                    "${filter.field}.keyword"
                                } else {
                                    filter.field
                                }
                                t.field(fieldName).terms { t1 ->
                                    t1.value(filter.values.map { v -> FieldValue.of(v) })
                                }
                            }
                        }
                    }
                    NOT_IN -> {
                        boolQuery.mustNot {
                            it.terms { t ->
                                t.field(filter.field).terms { t1 ->
                                    t1.value(filter.values.map { v -> FieldValue.of(v) })
                                }
                            }
                        }
                    }
                    else -> throw BadRequestException("Invalid Request. Please check the configuration and try again")
                }
            } else if (filter.value.isNotBlank()) {
                // Values only
                when (filter.operator) {
                    EQUAL_TO -> {
                        if (filter.value.isString()) {
                            boolQuery.must {
                                it.term { t ->
                                    t.field("${filter.field}.keyword")
                                    t.value(filter.value)
                                }
                            }
                        } else {
                            boolQuery.must {
                                it.term { t ->
                                    t.field(filter.field)
                                    t.value(filter.value)
                                }
                            }
                        }
                    }
                    NOT_EQUAL_TO -> {
                        if (filter.value.isString()) {
                            boolQuery.mustNot {
                                it.term { t ->
                                    t.field("${filter.field}.keyword")
                                    t.value(filter.value)
                                }
                            }
                        } else {
                            boolQuery.mustNot {
                                it.term { t ->
                                    t.field(filter.field)
                                    t.value(filter.value)
                                }
                            }
                        }
                    }
                    BETWEEN -> {
                        // Range Query. Both upper and lower bounds are Inclusive
                        if (filter.highValue.isNotBlank()) {
                            boolQuery.must {
                                it.range { r ->
                                    r.field(filter.field)
                                        .lte(JsonData.of(filter.highValue))
                                        .gte(JsonData.of(filter.value))
                                }
                            }
                        } else {
                            throw BadRequestException("highValue is required")
                        }
                    }
                    GREATER_THAN -> {
                        boolQuery.must {
                            it.range { r ->
                                r.field(filter.field)
                                    .gt(JsonData.of(filter.value))
                            }
                        }
                    }
                    GREATER_THAN_EQUAL_TO -> {
                        boolQuery.must {
                            it.range { r ->
                                r.field(filter.field)
                                    .gte(JsonData.of(filter.value))
                            }
                        }
                    }
                    LESS_THAN -> {
                        boolQuery.must {
                            it.range { r ->
                                r.field(filter.field)
                                    .lt(JsonData.of(filter.value))
                            }
                        }
                    }
                    LESS_THAN_EQUAL_TO -> {
                        boolQuery.must {
                            it.range { r ->
                                r.field(filter.field)
                                    .lte(JsonData.of(filter.value))
                            }
                        }
                    }
                    CONTAINS -> {
                        boolQuery.must {
                            it.match { m ->
                                m.field(filter.field).query(filter.value)
                            }
                        }
                    }
                    NOT_CONTAINS -> {
                        boolQuery.mustNot {
                            it.match { m ->
                                m.field(filter.field).query(filter.value)
                            }
                        }
                    }
                    else -> throw BadRequestException("Invalid Request. Please check the configuration and try again")
                }

            } else {
                throw BadRequestException("Bad Request!")
            }
        }

        return boolQuery.build()
    }

}

interface ElasticSearchService {
    fun getTimesheetWithConfiguration(configuration: Configuration): List<Map<String, Any>> = emptyList()
    fun getTimesheetBetweenDatesFilterByFields(userId: Long,
                                               startDate: Instant,
                                               endDate: Instant,
                                               vararg fields: String) = emptyList<TimesheetDocument>()

    fun getTimesheetOnScript(scriptId: String, fields: Map<String, String>) = emptyList<TimesheetDocument>()
    fun createStoredScript(scriptId: String, script: String): Boolean = false
}