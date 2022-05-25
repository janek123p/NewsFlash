package de.fhac.newsflash.data.controller

import android.content.Context
import androidx.room.Room
import de.fhac.newsflash.data.models.ISource
import de.fhac.newsflash.data.models.News
import de.fhac.newsflash.data.repositories.AppDatabase
import java.io.Closeable

object NewsController : Closeable {

    private var cached: MutableList<News> = mutableListOf();
    private var favorites: MutableList<News> = mutableListOf();

    /**
     * Load Database
     */
    init {

        for (i in 0..10) {
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
            "Der Fußball-Bundesligist Borussia Dortmund hat sich überraschend von seinem Cheftrainer Marco Rose getrennt. Dies sei das Ergebnis einer intensiven Saisonanalyse, teilte der BVB mit.",
            "https://www.tagesschau.de/regional/nordrheinwestfalen/bvb-trennt-sich-von-trainer-rose-105.html",
            "https://www.tagesschau.de/regional/nordrheinwestfalen/wdr-image-97465~_v-mittel16x9.jpg"
        );
    }

    fun getFavorites() = favorites

    fun getNews() = cached

    fun addFavorite(id: Int): Boolean {
        if (!cached.any { news -> news.id == id }) return false;

        return favorites.add(cached.first { news -> news.id == id })
    }

    fun removeFavorite(id: Int) = favorites.removeIf { news -> news.id == id };

    fun getNews(source: ISource): List<News> {
        var news = mutableListOf<News>();

        for (i in 0..10) {
            news += (News(
                i,
                "Nachricht $i von ${source.getName()}",
                "Ich bin die Beschreibung der Nachricht $i".repeat(3),
                source.getUrl().toString(),
                "https://upload.wikimedia.org/wikipedia/commons/thumb/1/11/Test-Logo.svg/783px-Test-Logo.svg.png"
            ));
        }

        return news;
    }


    /**
     * Cache all News to Database and close database
     */
    override fun close() {
        TODO("Not yet implemented")
    }
}