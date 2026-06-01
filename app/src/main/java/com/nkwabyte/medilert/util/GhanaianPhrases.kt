package com.nkwabyte.medilert.util

object GhanaianPhrases {

    /** Returns a time-of-day greeting in the selected language, or "" for English. */
    fun greeting(language: String, hour: Int): String {
        val period = when (hour) {
            in 0..11 -> "morning"
            in 12..16 -> "afternoon"
            else -> "evening"
        }
        return when (language) {
            "Akan / Twi" -> when (period) {
                "morning"   -> "Mema wo akye"
                "afternoon" -> "Mema wo aha"
                else        -> "Mema wo adwo"
            }
            "Ga"      -> when (period) {
                "morning"   -> "Ojekoo"
                "afternoon" -> "Ojee"
                else        -> "Odjoŋ"
            }
            "Ewe"     -> when (period) {
                "morning"   -> "Ŋdi"
                "afternoon" -> "Ŋdɔ"
                else        -> "Fiɖe"
            }
            "Dagbani" -> when (period) {
                "morning"   -> "Antiri"
                "afternoon" -> "Ambalim"
                else        -> "Zasim"
            }
            else -> ""
        }
    }

    /** A short wellness phrase shown subtly on key screens. */
    fun wellnessNote(language: String): String = when (language) {
        "Akan / Twi" -> "Wo ho ye"          // You are well
        "Ga"         -> "Miba nu"            // Stay healthy
        "Ewe"        -> "Ŋutifafa"           // Good health
        "Dagbani"    -> "Nba ŋun mali"       // Be healthy
        else         -> ""
    }

    /** Welcome phrase used on splash / onboarding. */
    fun welcome(language: String): String = when (language) {
        "Akan / Twi" -> "Akwaaba"            // Welcome
        "Ga"         -> "Ogboo"              // Welcome
        "Ewe"        -> "Woezor"             // Welcome
        "Dagbani"    -> "Despa"              // Welcome / Thank you
        else         -> ""
    }
}
