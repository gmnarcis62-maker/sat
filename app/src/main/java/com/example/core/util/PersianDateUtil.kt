package com.example.core.util

import java.util.Calendar
import java.util.Date

/**
 * High-precision Persian (Solar Hijri / جلالی) Calendar Utility.
 * Provides day of week, solar month names, and formatting.
 */
object PersianDateUtil {

    private val persianMonths = arrayOf(
        "فروردین", "اردیبهشت", "خرداد",
        "تیر", "مرداد", "شهریور",
        "مهر", "آبان", "آذر",
        "دی", "بهمن", "اسفند"
    )

    private val persianDaysOfWeek = arrayOf(
        "یکشنبه", "دوشنبه", "سه‌شنبه", "چهارشنبه", "پنج‌شنبه", "جمعه", "شنبه"
    )

    data class PersianDate(
        val year: Int,
        val month: Int,
        val day: Int,
        val monthName: String,
        val dayOfWeekName: String
    ) {
        fun format(separator: String = "/"): String {
            val m = if (month < 10) "0$month" else "$month"
            val d = if (day < 10) "0$day" else "$day"
            return "$year$separator$m$separator$d"
        }

        fun toFullPersianString(): String {
            return "$dayOfWeekName، $day $monthName $year"
        }
    }

    /**
     * Converts a Gregorian Date to Persian Solar Date.
     */
    fun gregorianToPersian(date: Date = Date()): PersianDate {
        val calendar = Calendar.getInstance()
        calendar.time = date

        val gYear = calendar.get(Calendar.YEAR)
        val gMonth = calendar.get(Calendar.MONTH) + 1
        val gDay = calendar.get(Calendar.DAY_OF_MONTH)
        val dayOfWeekIndex = calendar.get(Calendar.DAY_OF_WEEK) - 1 // 0 = Sunday

        val gDaysInMonth = intArrayOf(0, 31, 59, 90, 120, 151, 181, 212, 243, 273, 304, 334)
        var gy = gYear - 1600
        var gm = gMonth - 1
        var gd = gDay - 1

        var gDayNo = 365 * gy + ((gy + 3) / 4) - ((gy + 99) / 100) + ((gy + 399) / 400)
        gDayNo += gDaysInMonth[gm] + gd
        if (gm > 1 && ((gYear % 4 == 0 && gYear % 100 != 0) || (gYear % 400 == 0))) {
            gDayNo++
        }

        val jDayNo = gDayNo - 79
        val jNp = jDayNo / 12053
        val jDnRemainder = jDayNo % 12053

        var jy = 979 + 33 * jNp + 4 * (jDnRemainder / 1461)
        var jRemainder = jDnRemainder % 1461

        if (jRemainder >= 366) {
            jy += (jRemainder - 1) / 365
            jRemainder = (jRemainder - 1) % 365
        }

        val jm: Int
        val jd: Int
        if (jRemainder < 186) {
            jm = 1 + (jRemainder / 31)
            jd = 1 + (jRemainder % 31)
        } else {
            jm = 7 + ((jRemainder - 186) / 30)
            jd = 1 + ((jRemainder - 186) % 30)
        }

        val monthName = persianMonths[jm - 1]
        val dayOfWeek = persianDaysOfWeek[dayOfWeekIndex.coerceIn(0, 6)]

        return PersianDate(jy, jm, jd, monthName, dayOfWeek)
    }

    /**
     * Converts English digits to Persian numerals for localized UI display.
     */
    fun toPersianDigits(text: String): String {
        return text.replace('0', '۰')
            .replace('1', '۱')
            .replace('2', '۲')
            .replace('3', '۳')
            .replace('4', '۴')
            .replace('5', '۵')
            .replace('6', '۶')
            .replace('7', '۷')
            .replace('8', '۸')
            .replace('9', '۹')
    }
}
