package com.sleekydz86.paperlens.application.support

object DocumentTagSupport {

    private const val MAX_TAGS = 8
    private val whitespaceRegex = Regex("\\s+")
    private val separatorRegex = Regex("[,\n\r，]+")

    fun normalize(tags: List<String>): List<String> {
        val seen = linkedSetOf<String>()
        val normalized = mutableListOf<String>()

        tags.asSequence()
            .flatMap { separatorRegex.split(it).asSequence() }
            .map { sanitize(it) }
            .filter { it.isNotEmpty() }
            .forEach { tag ->
                val key = tag.lowercase()
                if (seen.add(key)) {
                    normalized += tag
                }
            }

        return normalized.take(MAX_TAGS)
    }

    fun resolve(manualTags: List<String>, suggestedKeywords: List<String>): List<String> {
        val normalizedManualTags = normalize(manualTags)
        if (normalizedManualTags.isNotEmpty()) return normalizedManualTags
        return normalize(suggestedKeywords)
    }

    private fun sanitize(raw: String): String =
        raw.replace("#", " ")
            .replace(whitespaceRegex, " ")
            .trim()
}
