package com.ottamotta.bank.account

import org.iban4j.Iban
import org.springframework.web.bind.annotation.*
import java.util.*

data class BankAccountDto(val iban: String, val accountType: String, val referenceAccount: String? = null) {
    companion object {
        fun fromBankAccount(bankAccount: BankAccount): BankAccountDto {
            return BankAccountDto(iban = bankAccount.iban.toString(),
                    accountType = bankAccount.type.typeName,
                    referenceAccount = bankAccount.properties.getReferenceCheckingAccount()?.toString())
        }
    }
}

@RestController
@RequestMapping("/accounts")
class AccountController(private val accountService: AccountService) {

    @PostMapping("/create/checking")
    fun createCheckingAccount() =
            BankAccountDto.fromBankAccount(accountService.createCheckingAccount())

    @PostMapping("/create/personalloan")
    fun createPersonalLoanAccount() =
            BankAccountDto.fromBankAccount(accountService.createPersonalLoanAccount())

    @PostMapping("/create/{reference_checking_account}/savings")
    fun createSavingsAccount(@PathVariable("reference_checking_account") reference: Iban) =
            BankAccountDto.fromBankAccount(accountService.createSavingsAccount(reference))

    @GetMapping
    fun getAll(@RequestParam("accountTypes") accountTypes: Optional<List<String>>): List<BankAccountDto> {
        val types = accountTypes.orElse(AccountType.values().map { it.typeName })
        return accountService.getAll()
                .filter { types.contains(it.type.typeName) }
                .map { BankAccountDto.fromBankAccount(it) }
    }

}