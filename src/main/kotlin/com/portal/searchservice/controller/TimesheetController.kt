package com.portal.searchservice.controller

import com.portal.searchservice.domain.Timesheet
import com.portal.searchservice.domain.TimesheetDocument
import com.portal.searchservice.dto.TimesheetDto
import com.portal.searchservice.mapper.TimesheetDocumentMapper
import com.portal.searchservice.mapper.TimesheetMapper
import com.portal.searchservice.service.TimesheetService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.time.Instant

@RestController
@RequestMapping("/timesheets")
class TimesheetControllerImpl(
    private val timesheetService: TimesheetService<Timesheet, TimesheetDocument>,
    private val timesheetMapper: TimesheetMapper,
    private val timesheetDocumentMapper: TimesheetDocumentMapper
) : TimeSheetController {

    @PostMapping
    override fun getTimesheetsWithConfig(
        @RequestBody elasticSearchScript: String
    ): ResponseEntity<Any> {


        return super.getTimesheetsWithConfig(elasticSearchScript)
    }

    @GetMapping
    override fun getTimesheetsBetween(
        @RequestParam("u") username: String,
        @RequestParam("sd") startDate: String,
        @RequestParam("ed") endDate: String,
        @RequestParam("p", defaultValue = "0") page: Int,
        @RequestParam("s", defaultValue = "10") size: Int
    ): ResponseEntity<Any> {
        val startDateInstant = Instant.parse(startDate)
        val endDateInstant = Instant.parse(endDate)

        val responseTimesheets = timesheetService.findTimesheetsBetween(username, startDateInstant, endDateInstant, page, size)
        val timesheetDto = responseTimesheets.map { timesheetDocumentMapper.toTimesheetDto(it) }

        return ResponseEntity(timesheetDto, HttpStatus.OK)
    }

    @GetMapping("/db")
    override fun getTimesheets(@RequestParam("u") username: String): ResponseEntity<Any> {
        val timesheetResponse: List<TimesheetDto> = timesheetService.getTimesheets(username).map { timesheet ->
            timesheetMapper.toTimesheetDto(timesheet)
        }
        return ResponseEntity(timesheetResponse, HttpStatus.OK)
    }

    @PostMapping("/add")
    override fun saveTimesheetEntry(
        @RequestParam("u") username: String,
        @RequestBody timesheetDto: TimesheetDto
    ): ResponseEntity<Any> {
        val timesheet = timesheetMapper.toTimesheet(timesheetDto)
        val savedTimesheet = timesheetService.saveTimesheetEntry(username, timesheet)
        val timesheetResponse = timesheetMapper.toTimesheetDto(savedTimesheet)

        return ResponseEntity(timesheetResponse, HttpStatus.OK)
    }
}


interface TimeSheetController {
    fun getTimesheetsWithConfig(elasticSearchScript: String): ResponseEntity<Any> = ResponseEntity(HttpStatus.NOT_IMPLEMENTED)
    fun getTimesheetsBetween(username: String, startDate: String, endDate: String, page: Int, size: Int): ResponseEntity<Any> = ResponseEntity(HttpStatus.NOT_IMPLEMENTED)
    fun getTimesheets(username: String): ResponseEntity<Any> = ResponseEntity(HttpStatus.NOT_IMPLEMENTED)
    fun saveTimesheetEntry(username: String, timesheetDto: TimesheetDto): ResponseEntity<Any> = ResponseEntity(HttpStatus.NOT_IMPLEMENTED)
}