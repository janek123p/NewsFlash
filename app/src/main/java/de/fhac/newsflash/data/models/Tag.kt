package de.fhac.newsflash.data.models

enum class Tag(val tagName: String, val keywords: List<String>) {
    ALL("Alle", listOf("Hello", "World", "!")),
}