package com.example.thisdayinhistory

import com.example.thisdayinhistory.ui.components.CodeScannerHelper
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

class ExampleUnitTest {
    @Test
    fun parseMonthAndDay_validMmDd() {
        val result = CodeScannerHelper.parseMonthAndDay("10-25")
        assertNotNull(result)
        assertEquals("10", result?.first)
        assertEquals("25", result?.second)
    }

    @Test
    fun parseMonthAndDay_singleDigitMonthDay() {
        val result = CodeScannerHelper.parseMonthAndDay("3-5")
        assertNotNull(result)
        assertEquals("03", result?.first)
        assertEquals("05", result?.second)
    }

    @Test
    fun parseMonthAndDay_fullYearDate() {
        val result = CodeScannerHelper.parseMonthAndDay("2024-07-04")
        assertNotNull(result)
        assertEquals("07", result?.first)
        assertEquals("04", result?.second)
    }

    @Test
    fun parseMonthAndDay_slashDelimiter() {
        val result = CodeScannerHelper.parseMonthAndDay("12/31")
        assertNotNull(result)
        assertEquals("12", result?.first)
        assertEquals("31", result?.second)
    }

    @Test
    fun parseMonthAndDay_invalidMonth() {
        val result = CodeScannerHelper.parseMonthAndDay("15-25")
        assertNull(result)
    }
}
