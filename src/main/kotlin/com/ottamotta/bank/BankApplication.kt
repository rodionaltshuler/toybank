package com.ottamotta.bank

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class BankApplication

fun main(args: Array<String>) {
	runApplication<BankApplication>(*args)
	ObjectMapper().registerModule(KotlinModule())
}
