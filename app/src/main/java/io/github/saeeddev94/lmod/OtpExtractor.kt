package io.github.saeeddev94.lmod

class OtpExtractor {
    companion object {
        fun extract(text: String): String? {
            return match(text, listOf("code")) ?:
              match(text, listOf("رمز", "پویا")) ?:
              match(text, listOf("عدد", "محرمانه")) ?:
              match(text, listOf("کد", "ورود")) ?:
              match(text, listOf("کد", "تایید", "شما"))
        }

        private fun regex(list: List<String>): Regex {
            val words = list.joinToString("\\s+")
            val pattern = "(?i)(#|{WORDS}\\s*:?)(\\s*)(\\d{4,8})"
            return pattern.replace("{WORDS}", words).toRegex()
        }

        private fun match(text: String, list: List<String>): String? {
            val regex = regex(list)
            val matchResult = regex.find(text)
            return matchResult?.groups?.get(3)?.value
        }
    }
}
