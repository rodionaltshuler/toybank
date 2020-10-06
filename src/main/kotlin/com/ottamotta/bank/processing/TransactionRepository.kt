package com.ottamotta.bank.processing

import org.iban4j.Iban

interface TransactionRepository {

    fun findAllForAccount(iban: Iban): List<Transaction>
    fun save(tx: Transaction): Transaction

}