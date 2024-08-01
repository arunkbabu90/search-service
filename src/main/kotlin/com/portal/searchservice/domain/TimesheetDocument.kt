package com.portal.searchservice.domain

import jakarta.persistence.Id
import org.springframework.data.elasticsearch.annotations.Document
import org.springframework.data.elasticsearch.annotations.Field
import org.springframework.data.elasticsearch.annotations.FieldType
import java.time.Instant

@Document(indexName = "hrms-*")
data class TimesheetDocument(
    @Id
    var id: Long = 0,

    @Field(name = "task", type = FieldType.Text)
    var task: String? = null,

    @Field(name = "hours", type = FieldType.Integer)
    var hours: Int = 0,

    @Field(name = "description", type = FieldType.Text)
    var description: String? = null,

    @Field(name = "timesheet_date", type = FieldType.Date)
    var timesheetDate: Instant? = null,

    @Field(name = "updated_at", type = FieldType.Date)
    var updatedAt: Instant? = null,

    @Field(name = "user_id", type = FieldType.Integer)
    var userId: Long = 0,

    @Field(name = "project_name", type = FieldType.Text)
    val projectName: String?,

    @Field(name = "type", type = FieldType.Text)
    val type: String?,

    @Field(name = "start_date", type = FieldType.Date)
    val startDate: Instant?,

    @Field(name = "end_date", type = FieldType.Date)
    val endDate: Instant?
)