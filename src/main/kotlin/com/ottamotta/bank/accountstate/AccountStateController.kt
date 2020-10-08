package com.ottamotta.bank.accountstate

import com.ottamotta.bank.account.Money
import com.ottamotta.bank.transactions.Transaction
import org.iban4j.Iban
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.time.Instant

@RestController
@RequestMapping("/accounts")
class AccountStateController(private val accountStateService: AccountStateService) {

    @GetMapping("/{iban}")
    fun getBalance(@PathVariable("iban") iban: Iban) =
            BalanceDto(iban.toString(), accountStateService.getBalance(iban))

    @GetMapping("/{iban}/history")
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

data class TransactionDto(val from: String, val to: String, val amount: Money, val timestamp: Instant) {
    companion object {
        const val CASH = "CASH"
        fun fromTransaction(tx: Transaction) =
                TransactionDto(to = tx.to?.toString() ?: CASH,
                        from = tx.from?.toString() ?: CASH,
                        amount = tx.amount,
                        timestamp = tx.timestamp)
    }
}

