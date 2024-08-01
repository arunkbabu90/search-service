package com.portal.searchservice.mapper

import com.portal.searchservice.domain.Timesheet
import com.portal.searchservice.domain.TimesheetDocument
import com.portal.searchservice.dto.TimesheetDto
import org.mapstruct.Mapper
import org.mapstruct.Mapping
import org.mapstruct.Mappings
import org.mapstruct.factory.Mappers

@Mapper(componentModel = "spring")
interface TimesheetDocumentMapper {
    companion object {
        val INSTANCE: TimesheetDocumentMapper = Mappers.getMapper(TimesheetDocumentMapper::class.java)
    }


    @Mappings(
        Mapping(target = "timesheetDateMillis", expression = "java(timesheet.getTimesheetDate().toEpochMilli())"),
        Mapping(target = "timesheetDate", expression = "java(timesheet.getTimesheetDate().toString())")
    )
    fun toTimesheetDto(timesheet: TimesheetDocument): TimesheetDto

    @Mappings(
        Mapping(target = "timesheetDate", expression = "java(java.time.Instant.ofEpochMilli(timesheetDto.getTimesheetDateMillis()))"),
    )
    fun toTimesheetDocument(timesheetDto: TimesheetDto): TimesheetDocument

    fun toTimesheet(timesheet: TimesheetDocument): Timesheet
}