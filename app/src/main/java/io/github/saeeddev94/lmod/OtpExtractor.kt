package io.github.saeeddev94.lmod

class OtpExtractor {
    companion object {
        fun extract(text: String): String? {
            val webOtp = "(?i)(?:#|code:\\s*)(\\d{4,8})".toRegex()
            val bankOtp = "(?i)(رمز\\s+پویا)(\\s*)(\\d{4,8})".toRegex()
            return match(text, webOtp) ?: match(text, bankOtp, 3)
        }

        private fun match(text: String, regex: Regex, group: Int = 1): String? {
            val matchResult = regex.find(text)
            return matchResult?.groups?.get(group)?.value
        }
    }
}
