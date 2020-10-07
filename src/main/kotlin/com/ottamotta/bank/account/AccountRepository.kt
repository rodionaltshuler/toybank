package com.ottamotta.bank.account

import org.iban4j.Iban

interface AccountRepository {

    fun findAll(): List<BankAccount>
    fun findById(iban: Iban): BankAccount?
    fun save(account: BankAccount): BankAccount
    fun clear()

}