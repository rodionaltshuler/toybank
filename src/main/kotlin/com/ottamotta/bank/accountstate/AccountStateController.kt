package com.ottamotta.bank.accountstate

import com.ottamotta.bank.account.Money
import com.ottamotta.bank.transactions.TransactionDto
import io.swagger.annotations.Api
import io.swagger.annotations.ApiOperation
import org.iban4j.Iban
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.time.Instant

@RestController
@RequestMapping("/accounts")
@Api(value="Account state", description="Getting balance or transactions history for accounts")
class AccountStateController(private val accountStateService: AccountStateService) {

    @GetMapping("/{iban}")
    @ApiOperation("Show balance for the account")
    fun getBalance(@PathVariable("iban") iban: Iban) =
            BalanceDto(iban.toString(), accountStateService.getBalance(iban))

    @GetMapping("/{iban}/history")
    @ApiOperation("Transaction history and current balance for the account")
    fun getTransactionHistory(@PathVariable("iban") iban: Iban) =
            TransactionHistory(
                    iban = iban.toString(),
                    balance = accountStateService.getBalance(iban),
                    timestamp = Instant.now(),
                    transactions = accountStateService.getTransactions(iban).map { TransactionDto.fromTransaction(it) }
            )

}

data class BalanceDto(val iban: String, val balance: Money, val timestamp: Instant = Instant.now())

data class TransactionHistory(val iban: String, val balance: Money, val timestamp: Instant, val transactions: List<TransactionDto>)

