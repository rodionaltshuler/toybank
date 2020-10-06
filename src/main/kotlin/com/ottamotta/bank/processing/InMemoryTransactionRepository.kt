package com.ottamotta.bank.processing

import org.iban4j.Iban
import org.springframework.stereotype.Repository

@Repository
class InMemoryTransactionRepository : TransactionRepository {

    private val transactions = ArrayList<Transaction>()

    override fun findAllForAccount(iban: Iban): List<Transaction> {
        return transactions.filter { it.to == iban || it.from == iban }
    }

    override fun save(tx: Transaction): Transaction {
        transactions.add(tx)
        return tx
    }

}