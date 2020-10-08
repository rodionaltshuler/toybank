package com.ottamotta.bank.account

import org.iban4j.Iban
import org.springframework.stereotype.Service

@Service
class AccountService(private val bankAccountFactory: BankAccountFactory,
                     private val accountRepository: AccountRepository) {

    fun createCheckingAccount(): BankAccount {
        val account = bankAccountFactory.createCheckingAccount()
        return accountRepository.save(account)
    }

    fun createSavingsAccount(referenceAccount: Iban): BankAccount {
        val account = bankAccountFactory.createSavingsAccount(referenceAccount = referenceAccount)
        return accountRepository.save(account)
    }

    fun createPersonalLoanAccount(): BankAccount {
        val account = bankAccountFactory.createPersonalLoanAccount()
        return accountRepository.save(account)
    }

    fun getAll() = accountRepository.findAll()

}