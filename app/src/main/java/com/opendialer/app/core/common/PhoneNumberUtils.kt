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

    private val charToT9Digit = mapOf(
        'a' to '2', 'b' to '2', 'c' to '2',
        'd' to '3', 'e' to '3', 'f' to '3',
        'g' to '4', 'h' to '4', 'i' to '4',
        'j' to '5', 'k' to '5', 'l' to '5',
        'm' to '6', 'n' to '6', 'o' to '6',
        'p' to '7', 'q' to '7', 'r' to '7', 's' to '7',
        't' to '8', 'u' to '8', 'v' to '8',
        'w' to '9', 'x' to '9', 'y' to '9', 'z' to '9'
    )

    fun nameToT9(name: String): String {
        val sb = StringBuilder(name.length)
        for (ch in name.lowercase(Locale.getDefault())) {
            val digit = charToT9Digit[ch]
            if (digit != null) {
                sb.append(digit)
            } else if (ch.isWhitespace()) {
                sb.append(' ')
            }
        }
        return sb.toString()
    }

    /**
     * Matches T9 query against contact name or number.
     * Returns a score: > 0 for match (higher score = better match), 0 for no match.
     */
    fun matchScoreT9(queryDigits: String, contactName: String, phoneNumber: String): Int {
        if (queryDigits.isEmpty()) return 100

        val normalizedPhone = normalize(phoneNumber)
        val cleanName = contactName.lowercase(Locale.getDefault()).trim()
        val t9Name = nameToT9(cleanName)

        // 1. Phone number match
        if (normalizedPhone.contains(queryDigits)) {
            return if (normalizedPhone.startsWith(queryDigits)) 90 else 50
        }

        if (t9Name.isEmpty()) return 0

        // 2. Direct name match (e.g. name starts with query)
        val words = t9Name.split("\\s+".toRegex())
        for (word in words) {
            if (word.startsWith(queryDigits)) return 100
        }

        // 3. Initials match (e.g. John Doe -> "53")
        val initials = StringBuilder()
        for (word in words) {
            if (word.isNotEmpty()) initials.append(word.first())
        }
        if (initials.toString().startsWith(queryDigits)) return 85

        // 4. Substring in name
        if (t9Name.contains(queryDigits)) return 60

        return 0
    }

    fun matchesT9(queryDigits: String, contactName: String): Boolean {
        return matchScoreT9(queryDigits, contactName, "") > 0
    }
}
