package com.portal.searchservice.dto

import com.fasterxml.jackson.annotation.JsonProperty

data class TimesheetDto(
    var project: String = "",
    var task: String = "",
    var hours: Int = 0,
    var description: String = "",
    @JsonProperty("timestamp") var timesheetDate: Long = 0
)