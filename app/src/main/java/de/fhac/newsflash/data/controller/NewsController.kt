package de.fhac.newsflash.data.controller

import android.content.Context
import androidx.room.Room
import de.fhac.newsflash.data.models.Filter
import de.fhac.newsflash.data.models.ISource
import de.fhac.newsflash.data.models.News
import de.fhac.newsflash.data.models.Tag
import de.fhac.newsflash.data.repositories.AppDatabase
import de.fhac.newsflash.data.service.RssService
import java.io.Closeable

object NewsController {

    private var cached: MutableList<News> = mutableListOf();
    private var favorites: MutableList<News> = mutableListOf();
    private var filter: Filter? = null;

    /**
     * Load Database
     */
    init {

    }

    fun setFilter(filter: Filter) {
        this.filter = filter;
    }

    fun resetFilter() {
        filter = null;
    }

    fun getFavorites() = favorites

    suspend fun getNews(refresh: Boolean = false): List<News> {
        if (cached.isEmpty() || refresh)
            refresh();

        return cached;
    }

    fun addFavorite(news: News): Boolean {
        if (!cached.contains(news)) return false;

        return favorites.add(news)
    }

    fun removeFavorite(news: News) = favorites.remove(news);


    private suspend fun refresh() {
        cached.clear();

        for (source in SourceController.getSources()) {
            if (filter != null && filter!!.sources.contains(source)) continue;

            val news = RssService.parseNews(source.getUrl());

            if (filter != null && filter!!.tags.isNotEmpty()) {
                cached.addAll(news.filter { news ->
                    filter!!.tags.any { tag ->
                        tag.keywords.any { s ->
                            news.name.contains(
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