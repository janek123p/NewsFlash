package de.fhac.newsflash.data.models

import java.net.URL

class News(val name: String, private val url: String, val description: String) {

    fun getUrl() : URL {
        return URL(url);
    }

}