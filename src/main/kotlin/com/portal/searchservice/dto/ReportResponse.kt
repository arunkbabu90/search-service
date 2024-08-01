package com.portal.searchservice.dto

import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.annotation.JsonNaming

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy::class)
data class ReportResponse(
    val statusMessage: String,
    val statusCode: Int,
    val totalHits: Int = 0,
    val showingHits: Int = 0,
    val timesheets: List<Map<String, Any>>
)
