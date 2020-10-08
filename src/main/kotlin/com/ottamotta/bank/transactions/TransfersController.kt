package com.ottamotta.bank.transactions

import com.ottamotta.bank.account.Money
import com.ottamotta.bank.transactions.processing.TransactionService
import org.iban4j.Iban
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/transfers")
class TransfersController(private val transactionService: TransactionService) {

    data class DepositRequest(val to: Iban, val amount: Money)

    @PostMapping("/deposit")
    fun deposit(@RequestBody request: DepositRequest) = TransactionDto.fromTransaction(
        transactionService.submit(TransferCommand(to = request.to, amount = request.amount))
    )

    data class WithdrawRequest(val from: Iban, val amount: Money)

    @PostMapping("/withdraw")
    fun withdraw(@RequestBody request: WithdrawRequest)= TransactionDto.fromTransaction(
        transactionService.submit(TransferCommand(from = request.from, amount = request.amount))
    )

    data class TransferRequest(val from: Iban, val to: Iban, val amount: Money)

    @PostMapping("/transfer")
    fun transfer(@RequestBody request: TransferRequest) = TransactionDto.fromTransaction(
         transactionService.submit(TransferCommand(from = request.from, to = request.to, amount = request.amount))
    )


}