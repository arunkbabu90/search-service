package com.portal.searchservice.dto

data class ReportResponse(
    val statusMessage: String,
    val statusCode: Int,
    val timesheets: List<Map<String, Any>>
)
