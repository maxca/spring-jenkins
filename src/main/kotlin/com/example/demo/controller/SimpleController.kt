package com.example.demo.controller

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

data class Order(
    val id: Int,
    val product: String,
    val quantity: Int,
    val price: Double
)

@RestController
@RequestMapping("/api")
class SimpleController {

    private val logger: Logger = LoggerFactory.getLogger(SimpleController::class.java)

    @GetMapping("/hello")
    fun sayHello(): Map<String, String> {
        logger.info("Handling /hello endpoint")
        return mapOf("message" to "Hello, World!")
    }

    @GetMapping("/orders")
    fun getOrders(): List<Order> {
        logger.info("Handling /orders endpoint")

        val orders = listOf(
            Order(id = 1, product = "Laptop", quantity = 1, price = 999.99),
            Order(id = 2, product = "Smartphone", quantity = 2, price = 499.99),
            Order(id = 3, product = "Headphones", quantity = 3, price = 199.99)
        )
        
        logger.info("Orders retrieved: $orders")
        return orders
    }
}
