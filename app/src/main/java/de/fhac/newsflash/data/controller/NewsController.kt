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

    fun init() {
        //Load cached favorites async
        GlobalScope.launch {
            var fav = AppDatabase.getDatabase()?.newsRepository()?.getAllFavorites()
            var mapped = fav?.map { databaseNewsWithSource -> databaseNewsWithSource.toNews() }

            favorites.addAll(mapped?.toMutableList() ?: mutableListOf())

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
            newsController.getSink().add(NewsEvent.NewsLoadingEvent());
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

        favoritesController.getSink().add(filtered(favorites));
        newsController.getSink().add(NewsEvent.NewsLoadedEvent(filtered(news)))
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
    suspend fun addFavorite(news: News): Boolean {
        if (favorites.contains(news)) return false;

        if (favorites.add(news)) {
            try {
                withContext(Dispatchers.Default) {
                    AppDatabase.getDatabase()?.newsRepository()?.insertOrUpdate(
                        news.toDatabase(true)
                    )
                };

                withContext(Dispatchers.Default){
                    var favs = AppDatabase.getDatabase()?.newsRepository()?.getAllFavorites();
                    println(favs?.size ?: "KEINE DATEN!")
                }
                favoritesController.getSink().add(favorites);
                return true
            } catch (e: Exception) {
                favorites.remove(news)
                errorController.getSink().add(
                    (errorController.getStream().getLatest() ?: mutableListOf()).plus(
                        Exception(
                            "Favorit konnten nicht in der Datenbank gespeichert werden",
                            e
                        )
                    )
                )
            }
        }
        return false
    }

    /**
     * Removes a news from the users favorites and database
     */
    suspend fun removeFavorite(news: News): Boolean {
        if (favorites.contains(news)) {
            var last = favorites[favorites.indexOf(news)];
            if (favorites.remove(last)) {
                try {
                    withContext(Dispatchers.Default) {
                        AppDatabase.getDatabase()?.newsRepository()?.delete(last.toDatabase(true))
                    }

                    favoritesController.getSink().add(favorites);
                    return true;
                } catch (e: Exception) {
                    favorites.add(news)
                    errorController.getSink().add(
                        (errorController.getStream().getLatest() ?: mutableListOf()).plus(
                            Exception(
                                "Favorit konnte nicht aus der Datenbank gelöscht werden",
                                e
                            )
                        )
                    )
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
        ) { //Init source controller if not happened
            SourceController.init();
        };

        for (source in SourceController.getSourceStream().getLatest()!!) { //Load news from sources
            try {
                val news = RssService.parseNews(source.getUrl());

                allNews.addAll(news.map { news ->
                    news.source = source; return@map news;
                }) //Add to allNews with source injected
            } catch (ex: Exception) {
                errors.add(
                    java.lang.Exception(
                        "Feed von ${source.getName()} konnte nicht geladen werden.",
                        ex
                    )
                )
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
        if(filter == null) return toFilter;
        var filter = this.filter!!.copy();

        if((filter!!.tags.isEmpty() && filter!!.sources.isEmpty())) return toFilter;

        var filtered = toFilter.toList();

        //If filter by sources filter by sources
        //Union of sources
        if (filter!!.sources.isNotEmpty()) {
            filtered = filtered.filter { news ->
                filter!!.sources.any { iSource ->
                    iSource.getUrl().equals(news.source?.getUrl(), true)
                }
            }
        }

        //If filter by tags. Filter descriptions and title by keyword of tags.
        //Intersection of tags
        if (filter!!.tags.isNotEmpty()) {
            filtered = filtered.filter { news ->
                (filter!!.tags.all { tag ->
                    tag.keywords.any { s ->
                        news.title.contains(getRegEx(s)) || news.description.contains(getRegEx(s))
                    }
                })
            }
        }

        return filtered;
    }

    private fun getRegEx(s: String) = Regex("[^A-Za-z0-9]${Regex.escape(s)}[^A-Za-z0-9]", RegexOption.IGNORE_CASE)


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