package de.fhac.newsflash.data.controller

import android.content.Context
import androidx.room.Room
import de.fhac.newsflash.data.models.Filter
import de.fhac.newsflash.data.models.ISource
import de.fhac.newsflash.data.models.News
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

        /*for (i in 0..10) {
            cached += News(
                i,
                "Nachricht $i",
                "Ich bin die Beschreibung der Nachricht $i".repeat(3),
                "https://google.com/",
                "https://upload.wikimedia.org/wikipedia/commons/thumb/1/11/Test-Logo.svg/783px-Test-Logo.svg.png"
            );

            favorites += News(
                i + 12,
                "Nachricht ${i + 12}",
                "Ich bin die Beschreibung der Nachricht ${i + 12}".repeat(3),
                "https://google.com/",
                "https://upload.wikimedia.org/wikipedia/commons/thumb/1/11/Test-Logo.svg/783px-Test-Logo.svg.png"
            );
        }

        cached += News(
            11,
            "Borussia Dortmund und Cheftrainer Marco Rose trennen sich",
            "Der Fußball-Bundesligist Borussia Dortmund hat sich überraschend von seinem Cheftrainer Marco Rose getrennt. Dies sei das Ergebnis einer intensiven Saisonanalyse, teilte der BVB mit. Dies sei das Ergebnis einer intensiven Saisonanalyse, teilte der BVB mit. Dies sei das Ergebnis einer intensiven Saisonanalyse, teilte der BVB mit. Dies sei das Ergebnis einer intensiven Saisonanalyse, teilte der BVB mit.",
            "https://www.tagesschau.de/regional/nordrheinwestfalen/bvb-trennt-sich-von-trainer-rose-105.html",
            "https://www.tagesschau.de/regional/nordrheinwestfalen/wdr-image-97465~_v-mittel16x9.jpg"
        );*/
    }

    fun setFilter(filter: Filter) {
        this.filter = filter;
    }

    fun resetFilter() {
        filter = null;
    }

    fun getFavorites() = favorites

    suspend fun getNews(): List<News> {
        if (cached.isEmpty())
            refresh();

        return cached;
    }

    fun addFavorite(id: Int): Boolean {
        if (!cached.any { news -> news.id == id }) return false;

        return favorites.add(cached.first { news -> news.id == id })
    }

    fun removeFavorite(id: Int) = favorites.removeIf { news -> news.id == id };


    private suspend fun refresh() {
        for (source in SourceController.getSources()) {
            if(filter != null && filter!!.sources.contains(source)) continue;

            cached.clear();

            var h = {news: List<News> -> cached = news.toMutableList()}

            cached.addAll(RssService.parse(source.getUrl().toString()));
        }
    }
}