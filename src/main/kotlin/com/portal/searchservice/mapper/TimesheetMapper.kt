package com.portal.searchservice.mapper

import com.portal.searchservice.domain.Timesheet
import com.portal.searchservice.dto.TimesheetDto
import org.mapstruct.Mapper
import org.mapstruct.Mapping
import org.mapstruct.Mappings
import org.mapstruct.factory.Mappers

@Mapper(componentModel = "spring")
interface TimesheetMapper {
    companion object {
        val INSTANCE: TimesheetMapper = Mappers.getMapper(TimesheetMapper::class.java)
    }

    @Mapping(target = "timesheetDate", expression = "java(timesheet.getTimesheetDate().toEpochMilli())")
    fun toTimesheetDto(timesheet: Timesheet): TimesheetDto

    @Mappings(
        Mapping(target = "timesheetDate", expression = "java(java.time.Instant.ofEpochMilli(timesheetDto.getTimesheetDate()))"),
        Mapping(target = "user", ignore = true)
    )
    fun toTimesheet(timesheetDto: TimesheetDto): Timesheet

//    @Named("instantToTimestamp")
//    fun instantToTimestamp(instant: Instant?): Long? = instant?.toEpochMilli()
//
//    @Named("timestampToInstant")
//    fun timestampToInstant(timestamp: Long?): Instant? = timestamp?.let { Instant.ofEpochMilli(it) }
}