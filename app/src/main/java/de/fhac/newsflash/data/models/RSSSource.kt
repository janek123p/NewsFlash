package de.fhac.newsflash.data.models

import java.net.URL

class RSSSource(private val name: String, private val url: String) : ISource {

    override fun getName(): String {
        return name;
    }

    override fun getUrl(): URL {
        return URL(this.url);
    }
}