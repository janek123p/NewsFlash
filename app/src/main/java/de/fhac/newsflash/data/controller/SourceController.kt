package de.fhac.newsflash.data.controller

import de.fhac.newsflash.data.models.ISource
import de.fhac.newsflash.data.models.RSSSource
import de.fhac.newsflash.data.service.RssService

object SourceController {

    private val sources = mutableListOf<ISource>(
        RSSSource("Tagesschau", "https://www.tagesschau.de/xml/rss2/"),
        RSSSource("Deutsche Welle", "https://rss.dw.com/xml/rss-de-all"),
        RSSSource("ZDF", "https://www.zdf.de/rss/zdf/nachrichten")
    )

    fun getSources() = sources;

    fun deleteSource(source: ISource) = sources.remove(source) != null;

    suspend fun registerSource(url: String): ISource? {
        try{
            val name = RssService.parseMeta(url);
            val source = RSSSource(name!!, url)

            sources.add(source);

            return source;
        }catch(ex: Exception){
            throw Exception(ex.message ?: "Ungültiger RSS Feed")
        }
    }
}