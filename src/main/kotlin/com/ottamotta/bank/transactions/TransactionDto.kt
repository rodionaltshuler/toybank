package com.ottamotta.bank.transactions

import com.ottamotta.bank.account.Money
import java.time.Instant

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