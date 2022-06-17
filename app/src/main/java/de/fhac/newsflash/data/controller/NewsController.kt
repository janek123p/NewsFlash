package de.fhac.newsflash.data.controller

import de.fhac.newsflash.data.models.Filter
import de.fhac.newsflash.data.models.News
import de.fhac.newsflash.data.service.RssService
import de.fhac.newsflash.data.stream.StreamSubscription.Stream.*
import kotlinx.coroutines.runBlocking
import java.lang.Exception

object NewsController {

    private val favorites: MutableList<News> = mutableListOf()

    private val newsController: StreamController<List<News>> = StreamController()
    private val favoritesController: StreamController<List<News>> = StreamController()
    private val errorController: StreamController<List<Exception>> = StreamController()

    /**
     * Load Database
     */
    init {

    }

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
        if (newsController.getStream().getLatest()?.contains(news) != true) return false;

        if (favorites.add(news)) {
            favoritesController.getSink().add(favorites)
            return true
        }
        return false
    }

    /**
     * Removes a news from the users favorites
     */
    fun removeFavorite(news: News): Boolean {
        if (favorites.remove(news)) {
            favoritesController.getSink().add(favorites);
            return true;
        }
        return false;
    };

    /**
     * Refreshes the newsfeed. Takes the specified filter into account.
     */
    suspend fun refresh(filter: Filter? = null, filterFavorites: Boolean? = false) {
        val filtered = mutableListOf<News>()
        var errors = mutableListOf<Exception>()

        if (SourceController.getSourceStream().getLatest() == null) {
            newsController.getSink().add(null)
            return
        };

        for (source in SourceController.getSourceStream().getLatest()!!) {
            if (filter != null && filter.sources.contains(source)) continue

            try {
                val news = RssService.parseNews(source.getUrl());

                if (filter != null && filter.tags.isNotEmpty()) {
                    filtered.addAll(news.filter { news ->
                        filter.tags.any { tag ->
                            tag.keywords.any { s ->
                                news.title.contains(
                                    s
                                ) || news.description.contains(s)
                            }
                        }
                    })
                    return
                }

                filtered.addAll(news)
            } catch (ex: Exception) {
                errors.add(ex)
            }
        }

        errorController.getSink().add(errors)
        newsController.getSink().add(filtered)
    }
}