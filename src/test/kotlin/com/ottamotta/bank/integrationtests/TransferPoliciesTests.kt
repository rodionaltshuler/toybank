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

@SpringBootTest
class TransferPoliciesTests {

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
    fun `NegativeAmountPolicy - negative amount is not allowed`() {
        val command = TransferCommand(to = checkingAccount.iban, from = CASH, amount = Money.valueOf(-1))
        assertThrows<PolicyNotSatisfiedException>() {
            transactionService.submit(command)
        }
    }

    @Test
    fun `SameAccountPolicy - Transfer is not allowed if to,from accounts in transaction are the same`() {
        val command = TransferCommand(to = checkingAccount.iban, from = checkingAccount.iban, amount = Money.valueOf(100))
        assertThrows<PolicyNotSatisfiedException>() {
            transactionService.submit(command)
        }
    }

    @Test
    fun `OverdraftPolicy - negative balance is not allowed`() {
        assert(accountStateService.getBalance(checkingAccount.iban) == Money.ZERO)
        val command = TransferCommand(to = CASH, from = checkingAccount.iban, amount = Money.valueOf(100))
        assertThrows<PolicyNotSatisfiedException>() {
            transactionService.submit(command)
        }
    }

    @Test
    fun `AccountExistsPolicy - Deposit is not allowed to non-existing account of our bank`() {

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
    fun `AccountInOurBankPolicy - Cash deposit is not allowed to other bank account`() {

        assert(!ibanService.belongsToOurBank(externalBankAccount))

        val depositAmount = Money.valueOf(250)
        val command = TransferCommand(to = externalBankAccount, from = CASH, amount = depositAmount)

        assertThrows<PolicyNotSatisfiedException>("Transactions should involve at least one account of our bank") {
            transactionService.submit(command)
        }
    }


}
