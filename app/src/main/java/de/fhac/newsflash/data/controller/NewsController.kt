package de.fhac.newsflash.data.controller

import de.fhac.newsflash.data.models.Filter
import de.fhac.newsflash.data.models.News
import de.fhac.newsflash.data.service.RssService
import kotlinx.coroutines.runBlocking

object NewsController {

    private var cached: MutableList<News> = mutableListOf();
    private var favorites: MutableList<News> = mutableListOf();
    private var filter: Filter? = null;

    /**
     * Load Database
     */
    init {

    }

    /**
     * Set the filter for sources and tags
     */
    fun setFilter(filter: Filter) {
        this.filter = filter;
    }

    /**
     * Remove the current filter
     */
    fun resetFilter() {
        filter = null;
    }

    /**
     * Get news marked as favorite
     */
    fun getFavorites() = favorites

    /**
     * Get all cached news, refresh if necessary
     *
     * @param refresh If true refreshed the newsfeed
     * @return All loaded news
     */
    suspend fun getNews(refresh: Boolean = false): List<News> {
        if (cached.isEmpty() || refresh)
            runBlocking {
                refresh();
            }

        return cached;
    }

    /**
     * Adds a news to the users favorites
     */
    fun addFavorite(news: News): Boolean {
        if (!cached.contains(news)) return false;

        return favorites.add(news)
    }

    /**
     * Removes a news from the users favorites
     */
    fun removeFavorite(news: News) = favorites.remove(news);

    /**
     * Refreshes the newsfeed. Takes the specified filter into account.
     */
    private suspend fun refresh() {
        cached.clear();

        for (source in SourceController.getSources()) {
            if (filter != null && filter!!.sources.contains(source)) continue;

            val news = RssService.parseNews(source.getUrl());

            if (filter != null && filter!!.tags.isNotEmpty()) {
                cached.addAll(news.filter { news ->
                    filter!!.tags.any { tag ->
                        tag.keywords.any { s ->
                            news.title.contains(
                                s
                            ) || news.description.contains(s)
                        }
                    }
                });
                return;
            }

            cached.addAll(news);
        }
    }
}