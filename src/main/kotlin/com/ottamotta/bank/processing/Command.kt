package com.ottamotta.bank.processing

import com.ottamotta.bank.account.Money
import org.iban4j.Iban

sealed class Command

val CASH: Iban? = null

class TransferCommand(val from: Iban? = CASH, val to: Iban? = CASH, val amount: Money): Command()