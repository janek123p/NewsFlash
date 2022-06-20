package de.fhac.newsflash.data.models

import kotlinx.parcelize.Parcelize
import java.net.URL

@Parcelize
class NewsApiSource(override val id: Long) : ISource {

    override fun getName(): String {
        return "News Api"
    }

    override fun getUrl(): String {
        return "https://newsapi.org"
    }
}