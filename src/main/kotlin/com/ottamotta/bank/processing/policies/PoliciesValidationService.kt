package com.ottamotta.bank.processing.policies

import com.ottamotta.bank.account.AccountRepository
import com.ottamotta.bank.account.IbanService
import com.ottamotta.bank.accountstate.AccountStateService
import com.ottamotta.bank.processing.Transaction
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
                    AccountInOurBankPolicy.apply(tx, ibanService),
                    AccountExistsPolicy.apply(tx, ibanService, accountRepository),
                    SameAccountPolicy.apply(tx),
                    OverdraftPolicy.apply(tx, ibanService, accountStateService),
                    DepositPolicy.apply(),
                    WithdrawalPolicy.apply(tx, ibanService, accountRepository)
            ).first{ !it.satisfied }
        } catch (ex: NoSuchElementException) {
            PolicyApplicationResult(satisfied = true)
        }
    }

}