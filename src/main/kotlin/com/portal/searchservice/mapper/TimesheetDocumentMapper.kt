package com.portal.searchservice.mapper

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

    @Mapping(target = "timesheetDate", expression = "java(timesheet.getTimesheetDate().toEpochMilli())")
    fun toTimesheetDto(timesheet: TimesheetDocument): TimesheetDto

    @Mappings(
        Mapping(target = "timesheetDate", expression = "java(java.time.Instant.ofEpochMilli(timesheetDto.getTimesheetDate()))"),
        Mapping(target = "user", ignore = true)
    )
    fun toTimesheet(timesheetDto: TimesheetDto): TimesheetDocument
}