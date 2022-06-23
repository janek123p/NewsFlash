package de.fhac.newsflash.data.controller

import de.fhac.newsflash.data.models.ISource
import de.fhac.newsflash.data.models.RSSSource
import de.fhac.newsflash.data.repositories.AppDatabase
import de.fhac.newsflash.data.repositories.models.DatabaseSource
import de.fhac.newsflash.data.service.RssService
import de.fhac.newsflash.data.stream.StreamSubscription.Stream.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

object SourceController {

    private var sources = mutableListOf<ISource>()

    private val sourceController = StreamController<MutableList<ISource>>();

    init {
        GlobalScope.launch {
            var feeds = AppDatabase.getDatabase()?.sourceRepository()?.getAll()
                ?.map { source -> RSSSource(source.uid!!, source.name, source.url) }
                ?.toMutableList<ISource>()

            if (feeds == null || feeds.isEmpty()) {
                AppDatabase.getDatabase()?.sourceRepository()?.insertAll(
                    DatabaseSource(
                        name = "Tagesschau",
                        url = "https://www.tagesschau.de/xml/rss2/"
                    ),
                    DatabaseSource(
                        name = "Deutsche Welle",
                        url = "https://rss.dw.com/xml/rss-de-all"
                    )
                )
            }

            feeds = AppDatabase.getDatabase()?.sourceRepository()?.getAll()
                ?.map { source -> source.toISource() }
                ?.toMutableList<ISource>()

            if (feeds != null && feeds.isNotEmpty())
                sources = feeds;

            sourceController.getSink().add(sources);
        }
    }

    /**
     * Get all configured sources
     */
    fun getSourceStream() = sourceController.getStream();

    /**
     * Delete a source
     */
    fun deleteSource(source: ISource, onError: ((java.lang.Exception) -> Unit)? = null) {
        if (sources.remove(source) != null) {
            GlobalScope.launch {
                try {
                    AppDatabase.getDatabase()?.sourceRepository()
                        ?.delete(DatabaseSource(source.id, source.getName(), source.getUrl()))
                } catch (ex: java.lang.Exception) {
                    if (onError != null)
                        onError(ex);
                }
            }

            sourceController.getSink().add(sources);
        }
    };

    /**
     * Register a new source by its url. Checks if its a valid rss feed and parses the feeds title.
     */
    suspend fun registerSource(url: String) {
        try {
            if (sources.any { source -> source.getUrl().equals(url, ignoreCase = false) })
                throw Exception("Feed ist bereits vorhanden!");

            val name = RssService.parseMeta(url);

            var id: Long? = AppDatabase.getDatabase()?.sourceRepository()
                ?.insert(DatabaseSource(null, name!!, url)) ?: return;

            val source = RSSSource(id!!, name!!, url)
            sources.add(source);

            sourceController.getSink().add(sources);
        } catch (ex: Exception) {
            throw Exception(ex.message ?: "Ungültiger RSS Feed")
        }
    }
}