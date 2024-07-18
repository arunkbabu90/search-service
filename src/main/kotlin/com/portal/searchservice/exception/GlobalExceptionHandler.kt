package com.portal.searchservice.exception

import co.elastic.clients.elasticsearch._types.ElasticsearchException
import com.portal.searchservice.dto.ErrorResponse
import org.springframework.data.elasticsearch.UncategorizedElasticsearchException
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.context.request.WebRequest

//@ControllerAdvice
class GlobalExceptionHandler {

    @ExceptionHandler(Exception::class)
    fun handleException(e: Exception, request: WebRequest) =
        ResponseEntity(
            ErrorResponse(
                statusMessage = "Internal Server Error",
                statusCode = HttpStatus.INTERNAL_SERVER_ERROR.value()
            ), HttpStatus.INTERNAL_SERVER_ERROR
        )

    @ExceptionHandler(ResourceNotFoundException::class)
    fun handleProductNotFound(e: ResourceNotFoundException, request: WebRequest) =
        ResponseEntity(
            ErrorResponse(
                statusMessage = e.message ?: "No Products Found",
                statusCode = HttpStatus.NOT_FOUND.value()
            ), HttpStatus.NOT_FOUND
        )

    @ExceptionHandler(ElasticsearchException::class)
    fun handleElasticsearchException(e: ElasticsearchException, request: WebRequest): ResponseEntity<ErrorResponse> {
        val msg = e.message ?: "Search Failed"
        val statMsg = if (msg.contains("[search_phase_execution_exception] all shards failed", ignoreCase = true)) {
            "No Search Results"
        } else {
            msg
        }

        return ResponseEntity(
            ErrorResponse(
                statusMessage = statMsg,
                statusCode = HttpStatus.NOT_FOUND.value()
            ), HttpStatus.NOT_FOUND
        )
    }

    @ExceptionHandler(UncategorizedElasticsearchException::class)
    fun handleUncategorizedElasticsearchException(e: UncategorizedElasticsearchException, request: WebRequest): ResponseEntity<ErrorResponse> {
        val msg = e.message ?: "Search Failed"
        val statMsg = if (msg.contains("[search_phase_execution_exception] all shards failed", ignoreCase = true)) {
            "No Search Results"
        } else {
            msg
        }

        return ResponseEntity(
            ErrorResponse(
                statusMessage = statMsg,
                statusCode = HttpStatus.NOT_FOUND.value()
            ), HttpStatus.NOT_FOUND
        )
    }
}