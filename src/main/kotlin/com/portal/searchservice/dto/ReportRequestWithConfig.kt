package com.portal.searchservice.dto

data class ReportRequestWithConfig(
    val username: String,
    val configuration: ConfigurationDto
)
