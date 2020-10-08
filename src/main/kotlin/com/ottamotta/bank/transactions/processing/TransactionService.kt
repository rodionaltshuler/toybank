package com.ottamotta.bank.transactions.processing

import com.ottamotta.bank.transactions.Command
import com.ottamotta.bank.transactions.Transaction
import com.ottamotta.bank.transactions.TransferCommand
import com.ottamotta.bank.transactions.processing.policies.*
import org.springframework.stereotype.Service

@Service
class TransactionService(private val transactionRepository: TransactionRepository,
                         private val policiesValidationService: PoliciesValidationService) {

    fun submit(command: Command): Transaction {
        val tx = when (command) {
            is TransferCommand -> Transaction(to = command.to, from = command.from, amount = command.amount)
        }

        val policiesValidationResult = policiesValidationService.apply(tx)

        if (!policiesValidationResult.satisfied) {
            throw PolicyNotSatisfiedException(policiesValidationResult.cause)
        }

        return transactionRepository.save(tx)
    }
}