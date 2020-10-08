package com.ottamotta.bank.transactions.processing

import com.ottamotta.bank.transactions.Transaction
import org.iban4j.Iban

interface TransactionRepository {

    fun findAllForAccount(iban: Iban): List<Transaction>
    fun save(tx: Transaction): Transaction
    fun clear()
}