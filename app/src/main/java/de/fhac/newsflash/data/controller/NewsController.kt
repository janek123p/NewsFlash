package de.fhac.newsflash.data.controller

import de.fhac.newsflash.data.models.Filter
import de.fhac.newsflash.data.models.News
import de.fhac.newsflash.data.repositories.AppDatabase
import de.fhac.newsflash.data.service.RssService
import de.fhac.newsflash.data.stream.StreamSubscription.Stream.*
import kotlinx.coroutines.*
import java.net.InetAddress
import java.util.*

/**
 * BLoC of the news component
 */
object NewsController {

    private val favorites: MutableList<News> = mutableListOf()
    private val news: MutableList<News> = mutableListOf()
    private var filter: Filter? = null;

    private val newsController: StreamController<NewsEvent> = StreamController()
    private val favoritesController: StreamController<List<News>> = StreamController()
    private val errorController: StreamController<List<Exception>> = StreamController()

    init {
        //Load cached favorites async
        GlobalScope.launch {
            favorites.addAll(
                AppDatabase.getDatabase()?.newsRepository()?.getAllFavorites()
                    ?.map { databaseNewsWithSource -> databaseNewsWithSource.toNews() }
                    ?.toMutableList()
                    ?: mutableListOf()
            )

            favoritesController.getSink().add(filtered(favorites));
        }
    }

    /**
     * Load all non favorite news cached in the db
     */
    fun loadCachedNews() {
        GlobalScope.launch {
            newsController.getSink().add(NewsEvent.NewsLoadingEvent());

            news.addAll(
                AppDatabase.getDatabase()?.newsRepository()?.getAllNonFavorites()
                    ?.map { databaseNewsWithSource -> databaseNewsWithSource.toNews() }
                    ?.toMutableList()
                    ?: mutableListOf()
            )

            newsController.getSink().add(NewsEvent.NewsLoadedEvent(filtered(news)));
        }
    }

    /**
     * Delete all cached non favorite news and save the lastest news
     */
    fun cacheNews() {
        try {
            var newsRepo = AppDatabase.getDatabase()?.newsRepository()

            newsRepo?.deleteAllNonFavorites();
            newsRepo?.insertAllIgnore(news.map { news -> news.toDatabase(false) }.toList())
        } catch (e: Exception) {

        }
    }

    /**
     * Set a new filter and filter news and favorites async.
     */
    fun setFilter(filter: Filter) {
        GlobalScope.launch {
            NewsController.filter = filter

            favoritesController.getSink().add(filtered(favorites));
            newsController.getSink().add(NewsEvent.NewsLoadedEvent(filtered(news)))
        }
    }

    /**
     * Clear the current filter
     */
    fun resetFilter() {
        filter = null;
    }

    /**
     * Get the current filter
     */
    fun getFilter() = filter;

    /**
     * Get occurring errors as stream
     */
    fun getErrorStream() = errorController.getStream()

    /**
     * Get news marked as favorite as stream
     */
    fun getFavoritesStream() = favoritesController.getStream()

    /**
     * Get all cached news, refresh if necessary
     *
     * @param refresh If true refreshed the newsfeed
     * @return All loaded news
     */
    fun getNewsStream() = newsController.getStream()

    /**
     * Adds a news to the users favorites and database
     */
    fun addFavorite(news: News): Boolean {
        if (!favorites.contains(news)) return false;

        if (favorites.add(news)) {
            try {
                AppDatabase.getDatabase()?.newsRepository()?.insertOrUpdate(
                    news.toDatabase(true)
                )
                favoritesController.getSink().add(favorites);
                return true
            } catch (e: Exception) {
                errorController.getSink().add(mutableListOf(e))
            }
        }
        return false
    }

    /**
     * Removes a news from the users favorites and database
     */
    fun removeFavorite(news: News): Boolean {
        if (favorites.contains(news)) {
            var last = favorites[favorites.indexOf(news)];
            if (favorites.remove(last)) {
                try {
                    AppDatabase.getDatabase()?.newsRepository()?.delete(last.toDatabase(true))
                    favoritesController.getSink().add(favorites);
                    return true;
                } catch (e: Exception) {
                    errorController.getSink().add(mutableListOf(e))
                }
            }
        }
        return false;
    };

    /**
     * Refreshes the news feed.
     *
     * If no sufficient internet connection is available loads cached news.
     * If internet connection is available reads specified rss feeds and saves loaded news to cache.
     */
    suspend fun refresh() {
        newsController.getSink()
            .add(NewsEvent.NewsLoadingEvent()) //Notify subscribers that loading is in progress

        //If no internet connection available load from cache
        if (!isInternetAvailable()) {
            if (news.isEmpty()) { //Load from db is no cached news available
                withContext(Dispatchers.Default) {
                    loadCachedNews()
                }
            }
            errorController.getSink()
                .add(listOf(java.lang.Exception("Keine Internetverbindung vorhanden"))) //Notify exception subscribers
            newsController.getSink()
                .add(NewsEvent.NewsLoadedEvent(filtered(news))) //Notify news subscribers
            return;
        }

        val allNews = mutableListOf<News>()
        var errors = mutableListOf<Exception>()

        if (SourceController.getSourceStream()
                .getLatest() == null
        ) { //If no sources no news can be loaded.
            newsController.getSink().add(NewsEvent.NewsLoadedEvent(null))
            return
        };

        for (source in SourceController.getSourceStream().getLatest()!!) { //Load news from sources
            try {
                if (source.getUrl().contains("cnn", true))
                    throw Exception("Hallo");

                val news = RssService.parseNews(source.getUrl());

                allNews.addAll(news.map { news ->
                    news.source = source; return@map news;
                }) //Add to allNews with source injected
            } catch (ex: Exception) {
                errors.add(ex)
            }
        }

        news.clear();
        news.addAll(allNews.distinctBy { news -> news.url }); //Allow only distinct urls

        errorController.getSink().add(errors)
        newsController.getSink().add(NewsEvent.NewsLoadedEvent(filtered(news)))

        GlobalScope.launch {
            cacheNews();
        }
    }

    /**
     * Filters the specified list of news by the injected filter.
     */
    private fun filtered(toFilter: List<News>): List<News> {
        if (filter == null || (filter!!.tags.isEmpty() && filter!!.sources.isEmpty())) return toFilter;

        var filtered = toFilter;

        if (filter!!.sources.isNotEmpty()) {
            filtered = filtered.filter { news ->
                filter!!.sources.any { iSource ->
                    iSource.getUrl().equals(news.source?.getUrl(), true)
                }
            }
        }

        if (filter!!.tags.isNotEmpty()) {
            filtered = filtered.filter { news ->
                (filter!!.tags.any { tag ->
                    tag.keywords.any { s ->
                        news.title.contains(
                            s
                        ) || news.description.contains(s)
                    }
                })
            }
        }

        return filtered;
    }

    /**
     * Checks if a sufficient internet connection is available.
     *
     * @author https://stackoverflow.com/questions/9570237/android-check-internet-connection
     */
    private fun isInternetAvailable(): Boolean {
        return try {
            val ipAddr: InetAddress = InetAddress.getByName("google.com")
            //You can replace it with your name
            !ipAddr.equals("")
        } catch (e: java.lang.Exception) {
            false
        }
    }
}