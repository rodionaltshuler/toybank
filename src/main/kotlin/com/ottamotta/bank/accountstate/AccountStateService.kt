package com.ottamotta.bank.accountstate

import com.ottamotta.bank.account.AccountRepository
import com.ottamotta.bank.account.Money
import com.ottamotta.bank.processing.Transaction
import com.ottamotta.bank.processing.TransactionRepository
import org.iban4j.Iban
import org.springframework.stereotype.Service

@Service
class AccountStateService(private val accountRepository: AccountRepository,
                          private val transactionRepository: TransactionRepository) {

    fun getBalance(iban: Iban): Money {
        return transactionRepository
                .findAllForAccount(iban)
                .foldRight(Money.ZERO, { tx, acc -> balanceCalculation(iban, tx, acc)})
    }

    private val balanceCalculation: (Iban, Transaction, Money) -> Money =
            {iban, tx, acc ->
                var balance: Money = Money.ZERO
                if (tx.from == iban) {
                    balance = acc - tx.amount
                }
                if (tx.to == iban) {
                    balance = acc + tx.amount
                }
                balance
    }
}