package com.opendialer.app.core.common

import com.google.i18n.phonenumbers.PhoneNumberUtil
import java.util.Locale

object PhoneNumberUtils {

    private val phoneUtil = PhoneNumberUtil.getInstance()

    fun normalize(number: String): String {
        return number.replace("[^0-9+]".toRegex(), "")
    }

    fun format(number: String, defaultCountryIso: String = Locale.getDefault().country): String {
        return try {
            val parsed = phoneUtil.parse(number, defaultCountryIso)
            phoneUtil.format(parsed, PhoneNumberUtil.PhoneNumberFormat.INTERNATIONAL)
        } catch (e: Exception) {
            number
        }
    }

    private val t9Map = mapOf(
        '2' to listOf('a', 'b', 'c'),
        '3' to listOf('d', 'e', 'f'),
        '4' to listOf('g', 'h', 'i'),
        '5' to listOf('j', 'k', 'l'),
        '6' to listOf('m', 'n', 'o'),
        '7' to listOf('p', 'q', 'r', 's'),
        '8' to listOf('t', 'u', 'v'),
        '9' to listOf('w', 'x', 'y', 'z')
    )

    fun matchesT9(queryDigits: String, contactName: String): Boolean {
        if (queryDigits.isEmpty()) return true
        val cleanName = contactName.lowercase(Locale.getDefault())
        val nameWords = cleanName.split("\\s+".toRegex())

        // Check if query matches beginning of full name or any word in name
        for (word in nameWords) {
            if (matchesWordPrefix(queryDigits, word)) return true
        }
        return false
    }

    private fun matchesWordPrefix(digits: String, word: String): Boolean {
        if (digits.length > word.length) return false
        for (i in digits.indices) {
            val digit = digits[i]
            val validChars = t9Map[digit] ?: return false
            if (word[i] !in validChars) return false
        }
        return true
    }
}
