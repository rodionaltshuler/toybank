package com.ottamotta.bank.integrationtests

import com.ottamotta.bank.account.*
import com.ottamotta.bank.accountstate.AccountStateService
import com.ottamotta.bank.transactions.CASH
import com.ottamotta.bank.transactions.processing.TransactionRepository
import com.ottamotta.bank.transactions.processing.TransactionService
import com.ottamotta.bank.transactions.TransferCommand
import org.iban4j.CountryCode
import org.iban4j.Iban
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest

@SpringBootTest
class CheckinAccountTests {

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
    fun `Deposit to checking account is possible from any account type which allows withdrawal from it`() {

        val amount = Money.valueOf(250)

        val otherCheckingAccount = bankAccountFactory.createCheckingAccount(ibanService.generate())
        accountRepository.save(otherCheckingAccount)

        val fundOtherCheckingAccountCommand = TransferCommand(from = CASH, to = otherCheckingAccount.iban, amount = amount)
        transactionService.submit(fundOtherCheckingAccountCommand)

        val fundSavingsAccountCommand = TransferCommand(from = CASH, to = savingAccount.iban, amount = amount)
        transactionService.submit(fundSavingsAccountCommand)
        assert(savingAccount.properties.getReferenceCheckingAccount() == checkingAccount.iban)

        listOf(otherCheckingAccount.iban, savingAccount.iban, externalBankAccount, CASH).forEach {
            val command = TransferCommand(from = it, to = checkingAccount.iban, amount = amount)
            val balanceBefore = accountStateService.getBalance(checkingAccount.iban)

            val validateOtherAccountBalance = it != CASH && ibanService.belongsToOurBank(it!!)

            if (validateOtherAccountBalance) {
                assert(accountStateService.getBalance(it!!) == amount)
            }

            transactionService.submit(command)

            val balanceAfter = accountStateService.getBalance(checkingAccount.iban)

            assert(balanceAfter == balanceBefore + amount)

            if (validateOtherAccountBalance) {
                assert(accountStateService.getBalance(it!!) == Money.ZERO)
            }
        }

    }


    @Test
    fun `Withdrawal from checking account is possible to any account type which allows deposit to it`() {

        val amount = Money.valueOf(250)

        val otherCheckingAccount = bankAccountFactory.createCheckingAccount(ibanService.generate())
        accountRepository.save(otherCheckingAccount)

        val fundCheckingAccountCommand = TransferCommand(from = CASH, to = checkingAccount.iban, amount = amount * Money.valueOf(10))
        transactionService.submit(fundCheckingAccountCommand)

        listOf(otherCheckingAccount.iban, savingAccount.iban, personalLoanAccount.iban, externalBankAccount, CASH).forEach {
            val command = TransferCommand(from = checkingAccount.iban, to = it, amount = amount)
            val balanceCheckingAccountBefore = accountStateService.getBalance(checkingAccount.iban)

            val validateOtherAccountBalance = it != CASH && ibanService.belongsToOurBank(it!!)

            if (validateOtherAccountBalance) {
                assert(accountStateService.getBalance(it!!) == Money.ZERO)
            }

            transactionService.submit(command)

            val balanceCheckingAccountAfter = accountStateService.getBalance(checkingAccount.iban)
            assert(balanceCheckingAccountAfter == balanceCheckingAccountBefore - amount)

            if (validateOtherAccountBalance) {
                assert(accountStateService.getBalance(it!!) == amount)
            }
        }


    }


    @Test
    fun `Cash deposit to specified bank account increases it's balance`() {

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
    fun `Cash withdrawal from specified bank account decreases it's balance`() {
        val iban = checkingAccount.iban

        //deposit funds
        val depositAmount = Money.valueOf(250)
        val depositCommand = TransferCommand(to = iban, from = CASH, amount = depositAmount)
        transactionService.submit(depositCommand)
        val initialBalance = accountStateService.getBalance(iban)

        //verify
        val withdrawalAmount = Money.valueOf(100)
        val withdrawalCommand = TransferCommand(from = iban, to = CASH, amount = withdrawalAmount)
        transactionService.submit(withdrawalCommand)
        val endBalance = accountStateService.getBalance(iban)

        assert(endBalance == depositAmount - withdrawalAmount)
    }
}
