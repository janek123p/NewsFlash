package de.fhac.newsflash.data.controller

import de.fhac.newsflash.data.models.ISource
import de.fhac.newsflash.data.models.RSSSource

class SourceController {

    private val sources = listOf<ISource>(RSSSource("Tagesschau", "https://www.tagesschau.de/xml/rss2/"), RSSSource("CNN", "http://rss.cnn.com/rss/edition.rss"), RSSSource("ZDF", "https://www.zdf.de/rss/zdf/nachrichten"))

    fun getSources() : List<ISource> {
        return sources;
    }


    fun addSource(source: ISource){
        sources.plus(source);
    }
}