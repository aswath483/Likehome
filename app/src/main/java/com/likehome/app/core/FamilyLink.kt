package com.likehome.app.core

import java.net.URLEncoder

/**
 * Messages to the real Mom on WhatsApp, through wa.me links: no account, no server, and
 * nothing leaves the phone until the user presses send in WhatsApp.
 */
object FamilyLink {

    /**
     * The number as wa.me wants it: country code and digits, no "+", "00" or spaces.
     * Null if it can't be a full international number (wa.me needs the country code).
     */
    fun normalise(number: String): String? {
        val trimmed = number.trim()
        var digits = trimmed.filter(Char::isDigit)
        if (!trimmed.startsWith("+") && digits.startsWith("00")) digits = digits.drop(2)
        return digits.takeIf { it.length in 8..15 && !it.startsWith("0") }
    }

    fun whatsApp(number: String, text: String): String? =
        normalise(number)?.let { "https://wa.me/$it?text=" + URLEncoder.encode(text, "UTF-8").replace("+", "%20") }
}
