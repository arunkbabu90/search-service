package com.portal.searchservice.exception

import com.portal.searchservice.dto.ErrorResponse
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.context.request.WebRequest

@ControllerAdvice
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
}