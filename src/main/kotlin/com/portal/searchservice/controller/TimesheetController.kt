package com.portal.searchservice.controller

import com.portal.searchservice.domain.Timesheet
import com.portal.searchservice.domain.TimesheetDocument
import com.portal.searchservice.dto.ReportRequest
import com.portal.searchservice.dto.ReportRequestWithConfig
import com.portal.searchservice.dto.ReportResponse
import com.portal.searchservice.dto.TimesheetDto
import com.portal.searchservice.service.TimesheetService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import utils.toConfiguration
import utils.toDto
import utils.toEntity
import java.time.Instant

@RestController
@RequestMapping("/timesheets")
class TimesheetControllerImpl(
    private val timesheetService: TimesheetService<Timesheet, TimesheetDocument>
) : TimeSheetController {

    @PostMapping("/report/with/config")
    override fun generateTimesheetReportWithConfig(@RequestBody body: ReportRequestWithConfig): ResponseEntity<Any> {
        val (username, configDto) = body
        val configuration = configDto.toConfiguration()
        val generatedTimesheetReport = timesheetService.generateTimesheetReportWithConfig(username, configuration)

        val response = ReportResponse(
            statusMessage = "Report Generated",
            statusCode = HttpStatus.OK.value(),
            username = username,
            timesheets = generatedTimesheetReport
        )

        return ResponseEntity(response, HttpStatus.OK)
    }

    @PostMapping("/report")
    override fun generateTimesheetReport(@RequestBody body: ReportRequest): ResponseEntity<Any> {
        val (username, startDate, endDate, columns) = body

        val startDateInstant = Instant.parse(startDate)
        val endDateInstant = Instant.parse(endDate)

        val generatedTimesheetReport = timesheetService.generateTimesheetReport(
            username = username,
            startDate = startDateInstant,
            endDate = endDateInstant,
            requiredColumns = columns.toTypedArray()
        )

        val response = ReportResponse(
            statusMessage = "Report Generated",
            statusCode = HttpStatus.OK.value(),
            username = username,
            timesheets = generatedTimesheetReport
        )

        return ResponseEntity(response, HttpStatus.OK)
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

        val timesheetDocuments = timesheetService.findTimesheetsBetween(username, startDateInstant, endDateInstant, page, size)
        val timesheetDto: List<TimesheetDto> = timesheetDocuments.map { timesheetDocument -> timesheetDocument.toDto() }

        return ResponseEntity(timesheetDto, HttpStatus.OK)
    }

    @GetMapping("/db")
    override fun getTimesheets(@RequestParam("u") username: String): ResponseEntity<Any> {
        val timesheetDtos: List<TimesheetDto> = timesheetService.getTimesheets(username).map { timesheet ->
            timesheet.toDto()
        }
        return ResponseEntity(timesheetDtos, HttpStatus.OK)
    }

    @PostMapping("/add")
    override fun saveTimesheetEntry(
        @RequestParam("u") username: String,
        @RequestBody timesheetDto: TimesheetDto
    ): ResponseEntity<Any> {
        val timesheet = timesheetDto.toEntity()
        val savedTimesheet: Timesheet = timesheetService.saveTimesheetEntry(username, timesheet)
        val timesheetResponse = savedTimesheet.toDto()

        return ResponseEntity(timesheetResponse, HttpStatus.OK)
    }
}


interface TimeSheetController {
    fun generateTimesheetReportWithConfig(body: ReportRequestWithConfig): ResponseEntity<Any> = ResponseEntity(HttpStatus.NOT_IMPLEMENTED)
    fun generateTimesheetReport(body: ReportRequest): ResponseEntity<Any> = ResponseEntity(HttpStatus.NOT_IMPLEMENTED)
    fun getTimesheetsBetween(username: String, startDate: String, endDate: String, page: Int, size: Int): ResponseEntity<Any> = ResponseEntity(HttpStatus.NOT_IMPLEMENTED)
    fun getTimesheets(username: String): ResponseEntity<Any> = ResponseEntity(HttpStatus.NOT_IMPLEMENTED)
    fun saveTimesheetEntry(username: String, timesheetDto: TimesheetDto): ResponseEntity<Any> = ResponseEntity(HttpStatus.NOT_IMPLEMENTED)
}