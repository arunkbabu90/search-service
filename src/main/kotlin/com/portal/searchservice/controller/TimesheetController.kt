package com.portal.searchservice.controller

import com.portal.searchservice.domain.Timesheet
import com.portal.searchservice.domain.TimesheetDocument
import com.portal.searchservice.dto.ConfigurationDto
import com.portal.searchservice.dto.ReportRequest
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
    override fun generateTimesheetReportWithConfig(@RequestBody body: ConfigurationDto): ResponseEntity<Any> {
        val configuration = body.toConfiguration()
        val generatedTimesheetReport = timesheetService.generateTimesheetReportWithConfig(configuration).toMutableList()
        val totalHits = generatedTimesheetReport.find { it.containsKey("total_hits") }?.get("total_hits") as Long
        generatedTimesheetReport.removeIf { it.containsKey("total_hits") }
        val hits = generatedTimesheetReport.size

        val response = ReportResponse(
            statusMessage = "Report Generated",
            statusCode = HttpStatus.OK.value(),
            totalHits = totalHits.toInt(),
            showingHits = hits,
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

    @GetMapping("/fun")
    override fun getTimesheetsUsingFunction(): ResponseEntity<Any> {
        val timesheets = timesheetService.getTimesheetsByFunction()
        println(timesheets)
        return ResponseEntity(timesheets, HttpStatus.OK)
    }

    @GetMapping("/fun/user")
    override fun getTimesheetsByUsernameUsingFunction(@RequestParam("u") username: String): ResponseEntity<Any> {
        val timesheets = timesheetService.getTimesheetsByUsernameByFunction(username)
        return ResponseEntity(timesheets, HttpStatus.OK)
    }

    @PostMapping("/report/with/config/fun")
    override fun generateTimesheetReportByConfiguration(@RequestBody body: ConfigurationDto): ResponseEntity<Any> {
        val configuration = body.toConfiguration()
        val timesheets = timesheetService.generateTimesheetReportByConfiguration(configuration)

        return ResponseEntity(timesheets, HttpStatus.OK)
    }
}


interface TimeSheetController {
    fun getTimesheetsByUsernameUsingFunction(username: String): ResponseEntity<Any> = ResponseEntity(HttpStatus.NOT_IMPLEMENTED)
    fun getTimesheetsUsingFunction(): ResponseEntity<Any> = ResponseEntity(HttpStatus.NOT_IMPLEMENTED)

    // TODO: Delete after fully implemented
    fun generateTimesheetReportByConfiguration(body: ConfigurationDto): ResponseEntity<Any> = ResponseEntity(HttpStatus.NOT_IMPLEMENTED)

    fun generateTimesheetReportWithConfig(body: ConfigurationDto): ResponseEntity<Any> = ResponseEntity(HttpStatus.NOT_IMPLEMENTED)
    fun generateTimesheetReport(body: ReportRequest): ResponseEntity<Any> = ResponseEntity(HttpStatus.NOT_IMPLEMENTED)
    fun getTimesheetsBetween(username: String, startDate: String, endDate: String, page: Int, size: Int): ResponseEntity<Any> = ResponseEntity(HttpStatus.NOT_IMPLEMENTED)
    fun getTimesheets(username: String): ResponseEntity<Any> = ResponseEntity(HttpStatus.NOT_IMPLEMENTED)
    fun saveTimesheetEntry(username: String, timesheetDto: TimesheetDto): ResponseEntity<Any> = ResponseEntity(HttpStatus.NOT_IMPLEMENTED)
}