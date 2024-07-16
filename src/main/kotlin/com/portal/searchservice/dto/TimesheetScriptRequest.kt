package com.portal.searchservice.dto

import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.annotation.JsonNaming

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy::class)
data class TimesheetScriptRequest(
    val scriptId: String,
    val fields: Map<String, String>
)
