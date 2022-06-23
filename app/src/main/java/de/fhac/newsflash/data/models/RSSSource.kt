package de.fhac.newsflash.data.models

import android.webkit.URLUtil
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.parcelize.Parcelize
import java.net.HttpURLConnection
import java.net.URL
import javax.xml.parsers.DocumentBuilderFactory

/**
 * Model representing a news source.
 *
 * @param id Internal id for database purposes
 * @param name Name of the source
 * @param url Link to the rss feed of the source
 */
@Parcelize
data class RSSSource(override val id: Long, private val name: String, private val url: String) :
    ISource {

    override fun getName(): String {
        return name;
    }

    override fun getUrl(): String {
        return url;
    }
}