package com.ivanz851.minesweeper.models

class User {
    private lateinit var name: String
    private lateinit var email: String
    private lateinit var password: String
    private lateinit var phone: String
    private var bestScore: Int = 0

    constructor() {
    }


    constructor(name: String, email: String, password: String, phone: String) {
        this.name = name
        this.email = email
        this.password = password
        this.phone = phone
    }


    fun getName(): String {
        return name
    }


    fun setName(name: String) {
        this.name = name
    }


    fun getEmail(): String {
        return email
    }


    fun setEmail(email: String) {
        this.email = email
    }


    fun getPassword(): String {
        return password
    }


    fun setPassword(password: String) {
        this.password = password
    }


    fun getPhone(): String {
        return phone
    }


    fun setPhone(phone: String) {
        this.phone = phone
    }


    fun getBestScore(): Int {
        return bestScore
    }


    fun setBestScore(bestScore: Int) {
        this.bestScore = bestScore
    }
}