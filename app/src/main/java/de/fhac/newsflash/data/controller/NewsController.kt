package de.fhac.newsflash.data.controller

import de.fhac.newsflash.data.models.Filter
import de.fhac.newsflash.data.models.News
import de.fhac.newsflash.data.repositories.AppDatabase
import de.fhac.newsflash.data.repositories.models.DatabaseNews
import de.fhac.newsflash.data.repositories.models.DatabaseSource
import de.fhac.newsflash.data.service.RssService
import de.fhac.newsflash.data.stream.StreamSubscription.Stream.*
import kotlinx.coroutines.runBlocking
import java.lang.Exception

object NewsController {

    private val favorites: MutableList<News> = mutableListOf()
    private val news: MutableList<News> = mutableListOf()
    private var filter: Filter? = null;

    private val newsController: StreamController<NewsEvent> = StreamController()
    private val favoritesController: StreamController<List<News>> = StreamController()
    private val errorController: StreamController<List<Exception>> = StreamController()

    /**
     * Load Database
     */
    init {

    }

    fun setFilter(filter: Filter) {
        this.filter = filter

        favoritesController.getSink().add(filtered(favorites));
        newsController.getSink().add(NewsEvent.NewsLoadedEvent(filtered(news)))
    }

    fun resetFilter() {
        filter = null;
    }

    fun getFilter() = filter;

    fun getErrorStream() = errorController.getStream()

    /**
     * Get news marked as favorite
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
     * Adds a news to the users favorites
     */
    fun addFavorite(news: News): Boolean {
        if (!favorites.contains(news)) return false;

        if (favorites.add(news)) {
            try {
                AppDatabase.getDatabase()?.newsRepository()?.insertOrUpdate(
                    news.toDatabase()
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
     * Removes a news from the users favorites
     */
    fun removeFavorite(news: News): Boolean {
        if (favorites.contains(news)) {
            var last = favorites[favorites.indexOf(news)];
            if(favorites.remove(last)) {
                try {
                    AppDatabase.getDatabase()?.newsRepository()?.delete(last.toDatabase())
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
     * Refreshes the newsfeed. Takes the specified filter into account.
     */
    suspend fun refresh() {
        newsController.getSink().add(NewsEvent.NewsLoadingEvent())
        val allNews = mutableListOf<News>()
        var errors = mutableListOf<Exception>()

        if (SourceController.getSourceStream().getLatest() == null) {
            newsController.getSink().add(null)
            return
        };

        for (source in SourceController.getSourceStream().getLatest()!!) {
            try {
                val news = RssService.parseNews(source.getUrl());

                allNews.addAll(news.map { news -> news.source = source; return@map news; })
            } catch (ex: Exception) {
                errors.add(ex)
            }
        }

        news.clear();
        news.addAll(allNews.distinctBy { news -> news.url });

        errorController.getSink().add(errors)
        newsController.getSink().add(NewsEvent.NewsLoadedEvent(filtered(news)))
    }

    private fun filtered(toFilter: List<News>): List<News> {
        if (filter == null || (filter!!.tags.isEmpty() && filter!!.sources.isEmpty())) return toFilter;

        return toFilter.filter { news ->
            filter?.sources?.any { iSource ->
                iSource.getUrl().equals(news.source?.getUrl(), true)
            } ?: true &&
                    (filter?.tags?.any { tag ->
                        tag.keywords.any { s ->
                            news.title.contains(
                                s
                            ) || news.description.contains(s)
                        }
                    } ?: true)
        }
    }
}