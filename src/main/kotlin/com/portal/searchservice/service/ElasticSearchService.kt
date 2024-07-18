package com.portal.searchservice.service

import com.portal.searchservice.domain.TimesheetDocument
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
}

interface ElasticSearchService {
    fun getTimesheetBetweenDatesFilterByFields(userId: Long,
                                               startDate: Instant,
                                               endDate: Instant,
                                               vararg fields: String) = listOf<TimesheetDocument>()

    fun getTimesheetOnScript(scriptId: String, fields: Map<String, String>) = listOf<TimesheetDocument>()
    fun createStoredScript(scriptId: String, script: String): Boolean = false
}