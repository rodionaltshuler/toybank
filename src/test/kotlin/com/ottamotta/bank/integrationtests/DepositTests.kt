package com.ottamotta.bank.integrationtests

import com.ottamotta.bank.account.*
import com.ottamotta.bank.accountstate.AccountStateService
import com.ottamotta.bank.processing.CASH
import com.ottamotta.bank.processing.TransactionRepository
import com.ottamotta.bank.processing.policies.PolicyNotSatisfiedException
import com.ottamotta.bank.processing.TransactionService
import com.ottamotta.bank.processing.TransferCommand
import org.iban4j.CountryCode
import org.iban4j.Iban
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest

@SpringBootTest
class DepositTests {

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
    fun `Depositing funds to specified bank account increases it's balance`() {

        val iban = checkingAccount.iban
        val initialBalance = accountStateService.getBalance(iban)

        //deposit funds
        val depositAmount = Money.valueOf(250)
        val command = TransferCommand(to = iban, from = CASH, amount = depositAmount)
        transactionService.submit(command)

        //verify
        val endBalance = accountStateService.getBalance(iban)
        assert(endBalance - initialBalance == depositAmount)
    }

    @Test
    fun `Deposit is not allowed to non-existing account of our bank`() {

        //create account
        val iban = ibanService.generate()
        val checkingAccount = bankAccountFactory.createCheckingAccount(iban)

        //verify account doesn't exist
        assert(accountRepository.findById(iban) == null)

        val depositAmount = Money.valueOf(250)
        val command = TransferCommand(to = iban, from = CASH, amount = depositAmount)

        assertThrows<PolicyNotSatisfiedException>("Account $iban doesn't exist") {
            transactionService.submit(command)
        }
    }

    @Test
    fun `Cash deposit is not allowed to other bank account`() {

        assert(!ibanService.belongsToOurBank(externalBankAccount))

        val depositAmount = Money.valueOf(250)
        val command = TransferCommand(to = externalBankAccount, from = CASH, amount = depositAmount)

        assertThrows<PolicyNotSatisfiedException>("Transactions should involve at least one account of our bank") {
            transactionService.submit(command)
        }
    }

    @Test
    fun `Can deposit money to savings account from all allowed account types (checking, cash, external bank)`() {
        val depositAmount = Money.valueOf(250)

        //fund checking account so we can transfer from it
        val fundCheckingAccountCommand = TransferCommand(from = CASH, to = checkingAccount.iban, amount = depositAmount)
        transactionService.submit(fundCheckingAccountCommand)

        listOf(null, checkingAccount.iban, externalBankAccount).forEach {
            val balanceBefore = accountStateService.getBalance(savingAccount.iban)
            val command = TransferCommand(from = it, to = savingAccount.iban, amount = depositAmount)
            transactionService.submit(command)
            val balanceAfter = accountStateService.getBalance(savingAccount.iban)
            assert(balanceAfter == balanceBefore + depositAmount)
        }
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

}
