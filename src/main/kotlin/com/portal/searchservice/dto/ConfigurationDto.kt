package com.portal.searchservice.dto

import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.annotation.JsonNaming

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy::class)
data class ConfigurationDto(
    var limit: Int = 0,
    var sorts: List<Sort> = emptyList(),
    var filters: List<Filter> = emptyList()
)

data class Sort(
    var field: String = "",
    var direction: String = ""
)

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy::class)
data class Filter(
    var field: String = "",
    private var _operator: String = "",
    var values: List<String> = emptyList(),
    var value: String = "",
    var highValue: String = ""
) {
    var operator: String
        get() = _operator.uppercase()
        set(value) {
            _operator = value
        }
}
