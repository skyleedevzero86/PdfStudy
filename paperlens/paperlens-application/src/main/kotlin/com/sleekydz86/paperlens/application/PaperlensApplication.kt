package com.sleekydz86.paperlens.application

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class PaperlensApplication

fun main(args: Array<String>) {
	runApplication<PaperlensApplication>(*args)
}
