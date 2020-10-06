package com.ottamotta.bank.processing

import org.springframework.stereotype.Service
import java.util.concurrent.CompletableFuture

@Service
class TransactionService(val transactionRepository: TransactionRepository) {

    fun submit(command: Command): Transaction {
        val tx = when(command) {
            is DepositCommand -> Transaction(to = command.to, from = null, amount = command.amount)
            is WithdrawalCommand -> Transaction(to = null, from = command.from, amount = command.amount)
            is TransferCommand -> Transaction(to = command.to, from = command.from, amount = command.amount)
        }
        //TODO apply tx validation policies here
        return transactionRepository.save(tx)
    }
}