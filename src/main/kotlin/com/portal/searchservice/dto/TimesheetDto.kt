package com.portal.searchservice.dto

import com.fasterxml.jackson.annotation.JsonProperty

data class TimesheetDto(
    var id: Int = 0,
    var userId: Long = 0,
    var project: String = "",
    var task: String = "",
    var hours: Int = 0,
    var description: String = "",
    var timesheetDate: String = "",
    @JsonProperty("timestamp") var timesheetDateMillis: Long = 0,

    var projectName: String = "",
    var type: String = "",
    var startDate: String = "",
    var endDate: String = ""
)