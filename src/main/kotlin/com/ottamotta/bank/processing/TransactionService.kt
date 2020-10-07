package com.ottamotta.bank.processing

import com.ottamotta.bank.processing.policies.*
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