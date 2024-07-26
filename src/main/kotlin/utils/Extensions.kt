package utils

import com.portal.searchservice.domain.Timesheet
import com.portal.searchservice.domain.TimesheetDocument
import com.portal.searchservice.dto.ConfigurationDto
import com.portal.searchservice.dto.TimesheetDto
import com.portal.searchservice.mapper.ConfigurationMapper
import com.portal.searchservice.mapper.TimesheetDocumentMapper
import com.portal.searchservice.mapper.TimesheetMapper
import kotlin.reflect.full.memberProperties

fun Timesheet.toDto() = TimesheetMapper.INSTANCE.toTimesheetDto(this)
fun TimesheetDocument.toDto() = TimesheetDocumentMapper.INSTANCE.toTimesheetDto(this)
fun TimesheetDocument.toEntity() = TimesheetDocumentMapper.INSTANCE.toTimesheet(this)
fun TimesheetDto.toEntity() = TimesheetMapper.INSTANCE.toTimesheet(this)
fun TimesheetDto.toESDocument() = TimesheetDocumentMapper.INSTANCE.toTimesheetDocument(this)
fun ConfigurationDto.toConfiguration() = ConfigurationMapper.INSTANCE.toConfiguration(this)

fun TimesheetDocument.toMap(): Map<String, Any> {
    return this::class.memberProperties
        .map { it.name to it.getter.call(this) }
        .filter { it.second != null && it.second != 0 && it.second != -1 } // Filter out null values, zeros, and -1
        .associate { it.first to it.second!! } // Map the property name to its value, ensuring no nulls
}