package com.ottamotta.bank.integrationtests

import com.ottamotta.bank.account.*
import com.ottamotta.bank.accountstate.AccountStateService
import com.ottamotta.bank.transactions.CASH
import com.ottamotta.bank.transactions.processing.TransactionRepository
import com.ottamotta.bank.transactions.processing.policies.PolicyNotSatisfiedException
import com.ottamotta.bank.transactions.processing.TransactionService
import com.ottamotta.bank.transactions.TransferCommand
import org.iban4j.CountryCode
import org.iban4j.Iban
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import java.math.BigDecimal

@SpringBootTest
class PersonalLoanAccountTests {

    @Autowired
    private lateinit var ibanService: IbanService

    @Autowired
    private lateinit var bankAccountFactory: BankAccountFactory

    @Autowired
    private lateinit var accountRepository: AccountRepository

    @Autowired
    private lateinit var accountStateService: AccountStateService

    @Autowired
    private lateinit var transactionRepository: TransactionRepository

    @Autowired
    private lateinit var transactionService: TransactionService

    private lateinit var checkingAccount: BankAccount
    private lateinit var savingAccount: BankAccount
    private lateinit var personalLoanAccount: BankAccount

    private lateinit var externalBankAccount: Iban

    @BeforeEach
    fun setup() {
        //clear old accounts and transactions
        accountRepository.clear()
        transactionRepository.clear()

        //create accounts
        checkingAccount = bankAccountFactory.createCheckingAccount(ibanService.generate())
        accountRepository.save(checkingAccount)

        personalLoanAccount = bankAccountFactory.createPersonalLoanAccount(ibanService.generate())
        accountRepository.save(personalLoanAccount)

        savingAccount = bankAccountFactory.createSavingsAccount(ibanService.generate(), checkingAccount.iban)
        accountRepository.save(savingAccount)

        externalBankAccount = Iban.Builder().countryCode(CountryCode.DE)
                .bankCode("12345678")
                .accountNumber("0000012345")
                .build()
    }

    @Test
    fun `Can deposit money to personal loan account from all allowed account types (checking, cash, external bank)`() {
        val depositAmount = Money.valueOf(250)

        //fund checking account so we can transfer from it
        val fundCheckingAccountCommand = TransferCommand(from = CASH, to = checkingAccount.iban, amount = depositAmount)
        transactionService.submit(fundCheckingAccountCommand)

        listOf(null, checkingAccount.iban, externalBankAccount).forEach {
            val balanceBefore = accountStateService.getBalance(personalLoanAccount.iban)
            val command = TransferCommand(from = it, to = personalLoanAccount.iban, amount = depositAmount)
            transactionService.submit(command)
            val balanceAfter = accountStateService.getBalance(personalLoanAccount.iban)
            assert(balanceAfter == balanceBefore + depositAmount)
        }
    }

    @Test
    fun `Withdrawal from personal loan is not possible to any of account types (checking, savings, personal loan, cash, external bank)`() {
        val amount = Money.valueOf(250)

        //fund personal loan account so we can transfer from it
        val fundPersonalLoanAccountCommand = TransferCommand(from = CASH, to = personalLoanAccount.iban, amount = amount * BigDecimal.valueOf(10))
        transactionService.submit(fundPersonalLoanAccountCommand)

        val otherPersonalLoanAccount = bankAccountFactory.createPersonalLoanAccount(ibanService.generate())
        accountRepository.save(otherPersonalLoanAccount)


        listOf(null, savingAccount.iban, checkingAccount.iban, otherPersonalLoanAccount.iban, externalBankAccount).forEach {
            val command = TransferCommand(from = personalLoanAccount.iban, to = it, amount = amount)
            val balanceBefore = accountStateService.getBalance(personalLoanAccount.iban)
            assertThrows<PolicyNotSatisfiedException>() {
                transactionService.submit(command)
            }
            val balanceAfter = accountStateService.getBalance(personalLoanAccount.iban)
            assert(balanceBefore == balanceAfter)
        }
    }


}
