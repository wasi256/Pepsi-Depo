package com.example.pepsi.common.util

private const val PASSWORD_CHARS = "ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnopqrstuvwxyz23456789!@#$%"

fun generatePassword(length: Int = 10): String =
    (1..length)
        .map { PASSWORD_CHARS.random() }
        .joinToString(separator = "")
