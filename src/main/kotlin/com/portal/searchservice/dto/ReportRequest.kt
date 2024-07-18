package com.portal.searchservice.dto

import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.annotation.JsonNaming

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy::class)
data class ReportRequest(
    val username: String,
    val startDate: String,
    val endDate: String,
    val columns: List<String>
)
