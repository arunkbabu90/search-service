package utils

import com.portal.searchservice.domain.Timesheet
import com.portal.searchservice.domain.TimesheetDocument
import com.portal.searchservice.dto.TimesheetDto
import com.portal.searchservice.mapper.TimesheetDocumentMapper
import com.portal.searchservice.mapper.TimesheetMapper

fun Timesheet.toDto() = TimesheetMapper.INSTANCE.toTimesheetDto(this)
fun TimesheetDocument.toDto() = TimesheetDocumentMapper.INSTANCE.toTimesheetDto(this)
fun TimesheetDto.toEntity() = TimesheetMapper.INSTANCE.toTimesheet(this)
fun TimesheetDto.toESDocument() = TimesheetDocumentMapper.INSTANCE.toTimesheetDocument(this)