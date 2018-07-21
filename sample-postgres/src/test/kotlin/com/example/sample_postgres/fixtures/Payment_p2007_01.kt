package com.example.sample_postgres.fixtures

// aaaaaaaaaaaaaaaa
// bbbbbbbbbbbbbbbb

import java.math.BigDecimal
import java.time.LocalDateTime
import com.ninja_squad.dbsetup_kotlin.DbSetupBuilder
import com.ninja_squad.dbsetup_kotlin.mappedValues

data class Payment_p2007_01 (
    val payment_id: Int = 0, 
    val customer_id: Short = 0, 
    val staff_id: Short = 0, 
    val rental_id: Int = 0, 
    val amount: BigDecimal = 0.toBigDecimal(), 
    val payment_date: LocalDateTime = LocalDateTime.now() 
)

fun DbSetupBuilder.insertPayment_p2007_01(f: Payment_p2007_01) {
    insertInto("payment_p2007_01") {
        mappedValues(
                "payment_id" to f.payment_id,
                "customer_id" to f.customer_id,
                "staff_id" to f.staff_id,
                "rental_id" to f.rental_id,
                "amount" to f.amount,
                "payment_date" to f.payment_date
        )
    }
}