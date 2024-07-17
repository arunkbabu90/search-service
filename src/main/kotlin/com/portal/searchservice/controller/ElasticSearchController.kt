package com.portal.searchservice.controller

import com.portal.searchservice.dto.StoreScript
import com.portal.searchservice.dto.TimesheetScriptRequest
import com.portal.searchservice.service.ElasticSearchService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import utils.toDto

@RestController
@RequestMapping("/es")
class ElasticSearchControllerImpl(
    private val elasticSearchService: ElasticSearchService
) : ElasticSearchController {

    @PostMapping("/stored-script/timesheets/get")
    override fun getTimesheetOnScript(
        @RequestBody timesheetScriptRequest: TimesheetScriptRequest
    ): ResponseEntity<Any> {
        val (scriptId, fields) = timesheetScriptRequest
        val timesheetDocuments = elasticSearchService.getTimesheetOnScript(scriptId, fields)
        val timesheetDtos = timesheetDocuments.map { timesheetDocument -> timesheetDocument.toDto() }

        return ResponseEntity(timesheetDtos, HttpStatus.OK)
    }

    @PostMapping("/stored-script")
    override fun createStoreScript(@RequestBody storeScript: StoreScript): ResponseEntity<Any> {
        val (scriptId, script) = storeScript
        val isCreated = elasticSearchService.createStoredScript(scriptId, script)
        return ResponseEntity(if (isCreated) HttpStatus.CREATED else HttpStatus.INTERNAL_SERVER_ERROR)
    }
}

interface ElasticSearchController {
    fun getTimesheetOnScript(timesheetScriptRequest: TimesheetScriptRequest): ResponseEntity<Any> =
        ResponseEntity(HttpStatus.NOT_IMPLEMENTED)

    fun createStoreScript(storeScript: StoreScript): ResponseEntity<Any> =
        ResponseEntity(HttpStatus.NOT_IMPLEMENTED)
}