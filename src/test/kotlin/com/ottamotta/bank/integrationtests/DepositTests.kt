package com.ottamotta.bank.integrationtests

import com.ottamotta.bank.account.BankAccountFactory
import com.ottamotta.bank.account.IbanGenerator
import com.ottamotta.bank.account.Money
import com.ottamotta.bank.accountstate.AccountStateService
import com.ottamotta.bank.account.AccountRepository
import com.ottamotta.bank.processing.DepositCommand
import com.ottamotta.bank.processing.TransactionService
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest

@SpringBootTest
class DepositTests {

    @Autowired
    private lateinit var ibanGenerator: IbanGenerator

    @Autowired
    private lateinit var bankAccountFactory: BankAccountFactory

    @Autowired
    private lateinit var accountRepository: AccountRepository

    @Autowired
    private lateinit var accountStateService: AccountStateService

    @Autowired
    private lateinit var transactionService: TransactionService

    @Test
    fun `depositing funds to specified bank account increases it's balance`() {

        //create account
        val iban = ibanGenerator.generate()
        val checkingAccount = bankAccountFactory.createCheckingAccount(iban)
        accountRepository.save(checkingAccount)

        val initialBalance = accountStateService.getBalance(iban)

        //deposit funds
        val depositAmount = Money.valueOf(250)
        val command = DepositCommand(to = iban, amount = depositAmount)
        transactionService.submit(command)

        //verify
        val endBalance = accountStateService.getBalance(iban)
        assert(endBalance - initialBalance == depositAmount)
    }

    @Test
    fun `can deposit money to savings account from any account type`() {
        //TODO
    }

    @Test
    fun `can deposit money to personal loan account from any account type`() {
        //TODO
    }

}
