package de.fhac.newsflash.data.models

import kotlinx.parcelize.Parcelize
import java.net.URL

@Parcelize
@Deprecated("Possibly removed on future version. Use RSSSource only!")
class NewsApiSource(override val id: Long) : ISource {

    override fun getName(): String {
        return "News Api"
    }

    override fun getUrl(): String {
        return "https://newsapi.org"
    }
}