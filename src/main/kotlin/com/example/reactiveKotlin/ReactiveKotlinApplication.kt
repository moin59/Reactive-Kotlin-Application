package com.example.reactiveKotlin

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class ReactiveKotlinApplication

fun main(args: Array<String>) {
	runApplication<ReactiveKotlinApplication>(*args)
}
