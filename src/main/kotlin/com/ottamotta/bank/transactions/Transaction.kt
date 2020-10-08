package com.ottamotta.bank.transactions

import com.ottamotta.bank.account.Money
import org.iban4j.Iban
import java.time.Instant
import java.util.*

data class Transaction(val id : String = UUID.randomUUID().toString(),
                       val from: Iban?,
                       val to: Iban?,
                       val amount: Money,
                       val timestamp: Instant = Instant.now())