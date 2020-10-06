package com.ottamotta.bank.processing

import com.ottamotta.bank.account.Money
import org.iban4j.Iban

sealed class Command

class DepositCommand(val to: Iban, val amount: Money): Command()

class WithdrawalCommand(val from: Iban, val amount: Money): Command()

class TransferCommand(val from: Iban, val to: Iban, val amount: Money): Command()