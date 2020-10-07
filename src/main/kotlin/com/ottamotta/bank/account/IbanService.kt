package com.ottamotta.bank.account

import org.iban4j.CountryCode
import org.iban4j.Iban
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import kotlin.math.pow
import kotlin.random.Random

@Component
class IbanService(@Value("\${bank.country}") private val countryCode: String,
                  @Value("\${bank.code}") private val bankCode: String,
                  private val accountNumberGenerator: AccountNumberGenerator) {

    companion object {
        const val ACCOUNT_NUMBER_LENGTH = 10
    }

    fun belongsToOurBank(iban: Iban) =
            iban.bankCode == bankCode && iban.countryCode.toString() == countryCode

    fun generate(): Iban {


        val accountNumber = accountNumberGenerator.generate(ACCOUNT_NUMBER_LENGTH)

        return Iban.Builder()
                .countryCode(CountryCode.getByCode(countryCode))
                .bankCode(bankCode)
                .accountNumber(String.format("%010d", accountNumber).take(ACCOUNT_NUMBER_LENGTH))
                .build()
    }
}

@Component
class AccountNumberGenerator {

    fun generate(length: Int) = Random.Default.nextLong(10.0.pow(length).toLong())

}