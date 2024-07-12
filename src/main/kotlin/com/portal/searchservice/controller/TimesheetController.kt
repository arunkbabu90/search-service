package com.portal.searchservice.controller

import com.portal.searchservice.domain.Timesheet
import com.portal.searchservice.dto.TimesheetDto
import com.portal.searchservice.mapper.TimesheetMapper
import com.portal.searchservice.service.TimesheetService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/timesheet")
class TimesheetControllerImpl(
    private val timesheetService: TimesheetService<Timesheet>,
    private val mapper: TimesheetMapper
) : TimeSheetController {

    @GetMapping
    override fun getTimesheets(@RequestParam("u") username: String): ResponseEntity<Any> {
        val timesheetResponse: List<TimesheetDto> = timesheetService.getTimesheets(username).map { timesheet ->
            mapper.toTimesheetDto(timesheet)
        }
        return ResponseEntity(timesheetResponse, HttpStatus.OK)
    }

    @PostMapping
    override fun saveTimesheetEntry(
        @RequestParam("u") username: String,
        @RequestBody timesheetDto: TimesheetDto
    ): ResponseEntity<Any> {
        val timesheet = mapper.toTimesheet(timesheetDto)
        val savedTimesheet = timesheetService.saveTimesheetEntry(username, timesheet)
        val timesheetResponse = mapper.toTimesheetDto(savedTimesheet)

        return ResponseEntity(timesheetResponse, HttpStatus.OK)
    }
}


interface TimeSheetController {
    fun getTimesheets(username: String): ResponseEntity<Any> = ResponseEntity(HttpStatus.NOT_IMPLEMENTED)
    fun saveTimesheetEntry(username: String, timesheetDto: TimesheetDto): ResponseEntity<Any> =
        ResponseEntity(HttpStatus.NOT_IMPLEMENTED)
}