package com.jawahir.amoro.util

fun Long.toFormattedCurrency(): String {
    if (this == 0L) return "N/A"
    return "$${"%,d".format(this)}"
}