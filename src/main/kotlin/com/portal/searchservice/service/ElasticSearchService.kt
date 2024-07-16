package com.portal.searchservice.service

import com.portal.searchservice.domain.TimesheetDocument
import org.springframework.data.elasticsearch.core.ElasticsearchOperations
import org.springframework.data.elasticsearch.core.query.SearchTemplateQuery
import org.springframework.data.elasticsearch.core.script.Script
import org.springframework.stereotype.Service

@Service
class ElasticSearchServiceImpl(private val operations: ElasticsearchOperations) : ElasticSearchService {

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
    fun getTimesheetOnScript(scriptId: String, fields: Map<String, String>): List<TimesheetDocument> = listOf()
    fun createStoredScript(scriptId: String, script: String): Boolean = false
}