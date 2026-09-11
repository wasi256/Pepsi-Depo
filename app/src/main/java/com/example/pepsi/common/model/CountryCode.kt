package com.example.pepsi.common.model

/**
 * A dialing code option for the Tel field. [localDigits] is how many digits the
 * national number must have (excluding the dial code) for that country.
 */
data class CountryCode(
    val countryName: String,
    val flagEmoji: String,
    val dialCode: String,
    val localDigits: Int,
) {
    val display: String get() = "$flagEmoji $dialCode"
}

val CountryCodes = listOf(
    CountryCode("Uganda", "🇺🇬", "+256", 10),
    CountryCode("Kenya", "🇰🇪", "+254", 9),
    CountryCode("Tanzania", "🇹🇿", "+255", 9),
    CountryCode("Rwanda", "🇷🇼", "+250", 9),
    CountryCode("South Sudan", "🇸🇸", "+211", 9),
    CountryCode("Nigeria", "🇳🇬", "+234", 10),
    CountryCode("DR Congo", "🇨🇩", "+243", 9),
)

val DefaultCountryCode = CountryCodes.first { it.countryName == "Uganda" }
