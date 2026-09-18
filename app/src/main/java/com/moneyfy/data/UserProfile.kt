package com.moneyfy.data

data class UserProfile(
    val username: String = "",
    val email: String = "",
    val securityQuestion: String = "",
    val securityAnswer: String = ""
)
