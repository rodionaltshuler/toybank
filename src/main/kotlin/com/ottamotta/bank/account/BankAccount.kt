package com.ottamotta.bank.account

import org.iban4j.Iban
import org.springframework.stereotype.Component

fun Map<String, Any>.getReferenceCheckingAccount() = this[BankAccount.REFERENCE_ACCOUNT_PROPERTY] as Iban

data class BankAccount(val iban: Iban, val type: AccountType, val properties: Map<String, Any> = emptyMap()) {
    companion object {
        const val REFERENCE_ACCOUNT_PROPERTY = "referenceAccount"
        fun createReferenceAccountProperty(iban: Iban) = mapOf(REFERENCE_ACCOUNT_PROPERTY to iban)
    }


}

@Component
class BankAccountFactory {

    fun createCheckingAccount(iban: Iban) = BankAccount(iban = iban, type = AccountType.CHECKING)

    fun createSavingsAccount(iban: Iban, referenceAccount: Iban): BankAccount {
        return BankAccount(iban = iban,
                type = AccountType.SAVINGS,
                properties = BankAccount.createReferenceAccountProperty(referenceAccount))
    }

    fun createPersonalLoanAccount(iban: Iban) = BankAccount(iban = iban, type = AccountType.PERSONAL_LOAN)

}

enum class AccountType(val typeName: String) {
    CHECKING("checking"),
    SAVINGS("savings"),
    PERSONAL_LOAN("personal loan")
}