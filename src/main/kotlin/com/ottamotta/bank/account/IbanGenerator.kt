package com.ottamotta.bank.account

import org.iban4j.CountryCode
import org.iban4j.Iban
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import kotlin.math.pow
import kotlin.random.Random

@Component
class IbanGenerator(@Value("\${bank.country}") private val countryCode: String,
                    @Value("\${bank.code}") private val bankCode: String) {

    fun generate(): Iban {

        val accountNumber = Random.Default.nextLong(10.0.pow(10.0).toLong())

        return Iban.Builder()
                .countryCode(CountryCode.getByCode(countryCode))
                .bankCode(bankCode)
                .accountNumber(accountNumber.toString())
                .build()
    }
}