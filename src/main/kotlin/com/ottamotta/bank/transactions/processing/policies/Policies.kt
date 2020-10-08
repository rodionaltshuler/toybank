package com.ottamotta.bank.transactions.processing.policies

import com.ottamotta.bank.account.*
import com.ottamotta.bank.transactions.CASH
import com.ottamotta.bank.transactions.Transaction
import org.iban4j.Iban

sealed class Policy

object NegativeAmountPolicy : Policy() {

    fun apply(transactionAmount: Money): PolicyApplicationResult {
        if (transactionAmount < Money.ZERO) {
            return PolicyApplicationResult(satisfied = false, cause = "Transactions with negative amount value are not supported")
        }
        return PolicyApplicationResult(satisfied = true)
    }

}

//Operation involves at least one account of our bank
//Deposit command is not allowed in external bank account
object AccountInOurBankPolicy : Policy() {

    fun apply(tx: Transaction, belongsToOurBank: (Iban) -> Boolean): PolicyApplicationResult {

        val accountsOfOurBank = listOf(tx.from, tx.to)
                .filter { it != CASH }
                .filter { iban ->
                    belongsToOurBank(iban!!)
                }.count()

        if (accountsOfOurBank == 0) {
            return PolicyApplicationResult(satisfied = false, cause = "Transactions should involve at least one account of our bank")
        }

        return PolicyApplicationResult(satisfied = true)
    }
}

object AccountExistsPolicy : Policy() {

    fun apply(tx: Transaction, belongsToOurBank: (Iban) -> Boolean, findAccount: (Iban) -> BankAccount?): PolicyApplicationResult {
        listOf(tx.from, tx.to).filter { it != CASH }.map { iban ->
            if (belongsToOurBank(iban!!)) {
                val account = findAccount(iban)
                if (account == null) {
                    return PolicyApplicationResult(satisfied = false, cause = "Account ${iban} doesn't exist")
                }
            }
        }
        return PolicyApplicationResult(satisfied = true)
    }
}

object SameAccountPolicy : Policy() {
    fun apply(tx: Transaction): PolicyApplicationResult {
        if (tx.from == tx.to) {
            return PolicyApplicationResult(satisfied = false, cause = "Transfer to the same account is not allowed")
        }
        return PolicyApplicationResult(satisfied = true)
    }
}

object OverdraftPolicy : Policy() {

    fun apply(tx: Transaction,  belongsToOurBank: (Iban) -> Boolean, getBalance: (Iban) -> Money): PolicyApplicationResult {
        if (tx.from != CASH && belongsToOurBank(tx.from!!)) {
            val expectedBalance = getBalance(tx.from) - tx.amount
            if (expectedBalance < Money.ZERO) {
                return PolicyApplicationResult(satisfied = false, cause = "Insufficient funds, overdraft is not allowed")
            }
        }
        if (tx.to != CASH && belongsToOurBank(tx.to!!)) {
            val expectedBalance = getBalance(tx.to) + tx.amount
            if (expectedBalance < Money.ZERO) {
                return PolicyApplicationResult(satisfied = false, cause = "Insufficient funds, overdraft is not allowed")
            }
        }
        return PolicyApplicationResult(satisfied = true)
    }
}

object DepositPolicy: Policy() {
    fun apply(): PolicyApplicationResult {
        //currently deposit is allowed to any bank account
        return PolicyApplicationResult(satisfied = true)
    }
}

object WithdrawalPolicy : Policy() {

    fun apply(tx: Transaction, belongsToOurBank: (Iban) -> Boolean, findAccount: (Iban) -> BankAccount?): PolicyApplicationResult {
        if (tx.from != CASH) {
            val withdrawalAccount = findAccount(tx.from!!)
            if (withdrawalAccount != null && belongsToOurBank(withdrawalAccount.iban)) {
                when (withdrawalAccount.type) {
                    AccountType.SAVINGS -> if (tx.to != withdrawalAccount.properties.getReferenceCheckingAccount()) {
                        return PolicyApplicationResult(satisfied = false, cause = "Withdrawal from savings account allowed only from reference checking account")
                    }
                    AccountType.PERSONAL_LOAN -> return PolicyApplicationResult(satisfied = false, cause = "Withdrawal from personal loan account is not allowed")
                    AccountType.CHECKING -> { //allowed }
                    }
                }
            }
        }
        return PolicyApplicationResult(satisfied = true)
    }
}

