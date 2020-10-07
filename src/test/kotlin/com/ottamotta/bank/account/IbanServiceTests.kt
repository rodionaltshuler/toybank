package com.ottamotta.bank.account

import io.mockk.every
import org.junit.jupiter.api.Test
import io.mockk.mockk
import kotlin.math.pow

class IbanServiceTests {

    @Test
    fun `Generates IBAN correctly for account number length specified`() {

        val accountNumberGenerator = mockk<AccountNumberGenerator>()
        val ibanGenerator = IbanService("DE", "12345678", accountNumberGenerator)

        IntRange(1, IbanService.ACCOUNT_NUMBER_LENGTH).forEach {

            val accountNumber = 10.0.pow(it - 1).toLong()
            assert(accountNumber.toString().length  == it)

            every {
                accountNumberGenerator.generate(any())
            } returns accountNumber

            val iban = ibanGenerator.generate()
            assert(iban.accountNumber.toLong() == accountNumber)
        }
    }


}