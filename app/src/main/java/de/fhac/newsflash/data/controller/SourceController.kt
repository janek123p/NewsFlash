package de.fhac.newsflash.data.controller

import de.fhac.newsflash.data.models.ISource
import de.fhac.newsflash.data.models.RSSSource
import de.fhac.newsflash.data.service.RssService

object SourceController {

    private val sources = mutableMapOf<Int, ISource>(
        0 to RSSSource(0, "Tagesschau", "https://www.tagesschau.de/xml/rss2/"),
        1 to RSSSource(1, "Deutsche Welle", "https://rss.dw.com/xml/rss-de-all"),
        2 to RSSSource(2, "ZDF", "https://www.zdf.de/rss/zdf/nachrichten")
    )

    fun getSources(): List<ISource> {
        return sources.values.toList()
    }

    fun deleteSource(id: Int) = sources.remove(id) != null;

    fun updateSource(id: Int, source: ISource): Boolean{
        if(!sources.containsKey(id)) return false;

        return sources.put(id, source) != null;
    }

    suspend fun registerSource(url: String): ISource? {
        val id = sources.maxOf { entry: Map.Entry<Int, ISource> -> entry.key } + 1;

        try{
            var name = RssService.parseMeta(url);
            sources[id] = RSSSource(id, name!!, url);
            return sources[id];
        }catch(ex: Exception){
            throw Exception(ex.message ?: "Ungültiger RSS Feed")
        }
    }
}