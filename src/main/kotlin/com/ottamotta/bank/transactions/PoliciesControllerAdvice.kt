package com.ottamotta.bank.transactions

import com.ottamotta.bank.transactions.processing.policies.PolicyNotSatisfiedException
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestControllerAdvice

@RestControllerAdvice
class PoliciesControllerAdvice {

    @ExceptionHandler(PolicyNotSatisfiedException::class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    fun noHandlerFoundException(ex: PolicyNotSatisfiedException) =
            ErrorResponse(message = ex.message ?: "Unknown policy violation exception")

    data class ErrorResponse(val success: Boolean = false, val message: String)

}