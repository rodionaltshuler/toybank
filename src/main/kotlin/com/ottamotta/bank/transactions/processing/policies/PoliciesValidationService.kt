package com.ottamotta.bank.transactions.processing.policies

import com.ottamotta.bank.account.AccountRepository
import com.ottamotta.bank.account.IbanService
import com.ottamotta.bank.accountstate.AccountStateService
import com.ottamotta.bank.transactions.Transaction
import org.springframework.stereotype.Service

data class PolicyApplicationResult(val satisfied: Boolean, val cause: String = "")

@Service
class PoliciesValidationService(private val accountRepository: AccountRepository,
                                private val accountStateService: AccountStateService,
                                private val ibanService: IbanService) {

    fun apply(tx: Transaction): PolicyApplicationResult {
        return try {
            listOf(
                    NegativeAmountPolicy.apply(tx.amount),
                    AccountInOurBankPolicy.apply(tx,  { iban -> ibanService.belongsToOurBank(iban)}),
                    AccountExistsPolicy.apply(tx, { iban -> ibanService.belongsToOurBank(iban)}, { iban -> accountRepository.findById(iban) }),
                    SameAccountPolicy.apply(tx),
                    OverdraftPolicy.apply(tx,  { iban -> ibanService.belongsToOurBank(iban)}, { iban -> accountStateService.getBalance(iban)}),
                    DepositPolicy.apply(),
                    WithdrawalPolicy.apply(tx, { iban -> ibanService.belongsToOurBank(iban)}, { iban -> accountRepository.findById(iban) })
            ).first{ !it.satisfied }
        } catch (ex: NoSuchElementException) {
            PolicyApplicationResult(satisfied = true)
        }
    }

}