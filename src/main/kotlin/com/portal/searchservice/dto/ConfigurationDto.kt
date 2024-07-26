package com.portal.searchservice.dto

import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.annotation.JsonNaming

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy::class)
data class ConfigurationDto(
    var limit: Int = 0,
    var sorts: List<Sort> = listOf(),
    var filters: List<Filter> = listOf()
)

data class Sort(
    var field: String = "",
    var direction: String = ""
)

data class Filter(
    var field: String = "",
    var values: List<String> = listOf(),
    var value: String = ""
)
