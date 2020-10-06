package com.ottamotta.bank.account

import org.iban4j.Iban
import org.springframework.stereotype.Repository

@Repository
class InMemoryAccountRepository: AccountRepository {

    private val accounts = HashMap<Iban, BankAccount>()

    override fun findAll(): List<BankAccount> {
        return accounts.values.toList()
    }

    override fun findById(iban: Iban): BankAccount? {
        return accounts[iban]
    }

    override fun save(account: BankAccount): BankAccount {
        accounts[account.iban] = account
        return accounts[account.iban]!!
    }
}