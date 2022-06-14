package de.fhac.newsflash.data.controller

import de.fhac.newsflash.data.models.ISource
import de.fhac.newsflash.data.models.RSSSource
import de.fhac.newsflash.data.service.RssService
import de.fhac.newsflash.data.stream.StreamSubscription.Stream.*

object SourceController {

    private val sources = mutableListOf<ISource>(
        RSSSource("Tagesschau", "https://www.tagesschau.de/xml/rss2/"),
        RSSSource("Deutsche Welle", "https://rss.dw.com/xml/rss-de-all"),
        RSSSource("ZDF", "https://www.zdf.de/rss/zdf/nachrichten")
    )

    private val sourceController = StreamController<MutableList<ISource>>();

    /**
     * Get all configured sources
     */
    fun getSourceStream() = sourceController.getStream();

    /**
     * Delete a source
     */
    fun deleteSource(source: ISource) : Boolean{
        if(sources.remove(source) != null){
            sourceController.getSink().add(sources);
            return true;
        }
        return false;
    };

    /**
     * Register a new source by its url. Checks if its a valid rss feed and parses the feeds title.
     */
    suspend fun registerSource(url: String) {
        try{
            val name = RssService.parseMeta(url);
            val source = RSSSource(name!!, url)

            sources.add(source);

            sourceController.getSink().add(sources);
        }catch(ex: Exception){
            throw Exception(ex.message ?: "Ungültiger RSS Feed")
        }
    }
}