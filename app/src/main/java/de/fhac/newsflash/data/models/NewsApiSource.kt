package de.fhac.newsflash.data.models

import java.net.URL

class NewsApiSource : ISource {

    override fun getName(): String {
        return "News Api";
    }

    override fun getUrl(): URL {
        return URL("https://newsapi.org");
    }
}