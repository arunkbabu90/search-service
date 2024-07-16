package com.portal.searchservice.repository

import com.portal.searchservice.domain.TimesheetDocument
import org.springframework.data.elasticsearch.core.SearchPage

interface TimesheetSearchRepository {
    fun findByTemplate()
    fun findByUserIdBetween(): SearchPage<TimesheetDocument>
}