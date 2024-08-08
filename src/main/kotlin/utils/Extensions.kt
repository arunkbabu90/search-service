package utils

import co.elastic.clients.elasticsearch._types.SortOrder
import com.portal.searchservice.domain.Timesheet
import com.portal.searchservice.domain.TimesheetDocument
import com.portal.searchservice.dto.ConfigurationDto
import com.portal.searchservice.dto.TimesheetDto
import com.portal.searchservice.mapper.ConfigurationMapper
import com.portal.searchservice.mapper.TimesheetDocumentMapper
import com.portal.searchservice.mapper.TimesheetMapper
import java.time.Instant
import java.time.format.DateTimeParseException
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
        .filter { it.second != null && it.second != 0 && it.second != -1 }
        .associate { it.first to it.second!! }
}

fun Timesheet.toMap(): Map<String, Any> {
    return this::class.memberProperties
        .map { it.name to it.getter.call(this) }
        .filter { it.second != null && it.second != 0 && it.second != -1 }
        .associate { it.first to it.second!! }
}

fun String.isNumber(): Boolean {
    return this.toIntOrNull() != null || this.toDoubleOrNull() != null || this.toFloatOrNull() != null
            || this.toLongOrNull() != null || this.toBigDecimalOrNull() != null || this.toBigIntegerOrNull() != null
}
fun String.isNotNumber(): Boolean = !isNumber()

fun String.isBoolean(): Boolean = this.equals("true", ignoreCase = true) || this.equals("false", ignoreCase = true)
fun String.isNotBoolean(): Boolean = !isBoolean()

fun String.isDate(): Boolean {
    try {
        Instant.parse(this)
        return true
    } catch (e: DateTimeParseException) {
        return false
    }
}
fun String.isNotDate(): Boolean = !isDate()

fun String.isString(): Boolean = isNotBoolean() && isNotNumber() && isNotDate()
fun String.isNotString(): Boolean = !isString()

fun String.toSortOrder(): SortOrder {
    return when (this) {
        "asc", "ascending" -> SortOrder.Asc
        "desc", "descending" -> SortOrder.Desc
        else -> throw IllegalArgumentException("Invalid Sort Order")
    }
}