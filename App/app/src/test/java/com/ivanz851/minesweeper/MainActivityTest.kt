package com.ivanz851.minesweeper

import org.junit.Assert.*

import org.junit.Test

class MainActivityTest {

    @Test
    fun testIsEmailValid() {
        assertTrue(MainActivity().isEmailValid("example@example.com"))
        assertTrue(MainActivity().isEmailValid("example123@example.co.uk"))
        assertTrue(MainActivity().isEmailValid("example.123@example.co.uk"))
        assertFalse(MainActivity().isEmailValid("example@examplecom"))
        assertFalse(MainActivity().isEmailValid("exampleexample.com"))
        assertFalse(MainActivity().isEmailValid("exam@ple@example.com"))
        assertFalse(MainActivity().isEmailValid("example@"))
    }

    @Test
    fun testIsPhoneNumberValid() {
        assertTrue(MainActivity().isPhoneNumberValid("1234567890"))
        assertTrue(MainActivity().isPhoneNumberValid("123-456-7890"))
        assertTrue(MainActivity().isPhoneNumberValid("(123) 456-7890"))
        assertTrue(MainActivity().isPhoneNumberValid("+7 (123) 456-7890"))
        assertFalse(MainActivity().isPhoneNumberValid("123"))
        assertFalse(MainActivity().isPhoneNumberValid("12345678901"))
        assertFalse(MainActivity().isPhoneNumberValid("123.456.7890"))
    }

    @Test
    fun testIsPasswordValid() {
        assertTrue(MainActivity().isPasswordValid("Password123!"))
        assertTrue(MainActivity().isPasswordValid("Pass@word123"))
        assertFalse(MainActivity().isPasswordValid("password"))
        assertFalse(MainActivity().isPasswordValid("12345678"))
        assertFalse(MainActivity().isPasswordValid("!@#$%^&*()"))
        assertFalse(MainActivity().isPasswordValid("Pass123"))
        assertFalse(MainActivity().isPasswordValid("P@ss"))
    }
}