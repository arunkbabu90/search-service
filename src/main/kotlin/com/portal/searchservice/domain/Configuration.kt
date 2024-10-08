package com.portal.searchservice.domain

import com.portal.searchservice.dto.Filter
import com.portal.searchservice.dto.Sort

data class Configuration(
    var limit: Int = 0,
    var sorts: List<Sort> = emptyList(),
    var filters: List<Filter> = emptyList()
)
