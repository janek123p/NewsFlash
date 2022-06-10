package de.fhac.newsflash.data.controller

import de.fhac.newsflash.data.models.ISource
import de.fhac.newsflash.data.models.RSSSource

object SourceController {

    private val sources = mutableMapOf<Int, ISource>(
        0 to RSSSource(0, "Tagesschau", "https://www.tagesschau.de/xml/rss2/"),
//        1 to RSSSource(1, "CNN", "http://rss.cnn.com/rss/edition.rss"),
//        2 to RSSSource(2, "ZDF", "https://www.zdf.de/rss/zdf/nachrichten")
    )

    fun getSources(): List<ISource> {
        return sources.values.toList()
    }

    fun deleteSource(id: Int) = sources.remove(id) != null;

    fun updateSource(id: Int, source: ISource): Boolean{
        if(!sources.containsKey(id)) return false;

        return sources.put(id, source) != null;
    }

    fun registerSource(source: ISource): Int {
        val id = sources.maxOf { entry: Map.Entry<Int, ISource> -> entry.key } + 1;
        sources[id] = source;
        return id;
    }
}