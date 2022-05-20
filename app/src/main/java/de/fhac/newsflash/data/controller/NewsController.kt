package de.fhac.newsflash.data.controller

import de.fhac.newsflash.data.models.ISource
import de.fhac.newsflash.data.models.News

class NewsController {

    fun getNews(): List<News> {
        var news = emptyList<News>();

        for (i in 0..10) {
            news += News(
                "Nachricht $i",
                "Ich bin die Beschreibung der Nachricht $i".repeat(3),
                "https://google.com/",
                "https://upload.wikimedia.org/wikipedia/commons/thumb/1/11/Test-Logo.svg/783px-Test-Logo.svg.png"
            );
        }

        news += News(
            "Borussia Dortmund und Cheftrainer Marco Rose trennen sich",
            "Der Fußball-Bundesligist Borussia Dortmund hat sich überraschend von seinem Cheftrainer Marco Rose getrennt. Dies sei das Ergebnis einer intensiven Saisonanalyse, teilte der BVB mit.",
            "https://www.tagesschau.de/regional/nordrheinwestfalen/bvb-trennt-sich-von-trainer-rose-105.html",
            "https://www.tagesschau.de/regional/nordrheinwestfalen/wdr-image-97465~_v-mittel16x9.jpg"
        );

        return news;
    }

    fun getNews(source: ISource): List<News> {
        var news = emptyList<News>();

        for (i in 0..10) {
            news += (News(
                "Nachricht $i von ${source.getName()}",
                "Ich bin die Beschreibung der Nachricht $i".repeat(3),
                source.getUrl().toString(),
                "https://upload.wikimedia.org/wikipedia/commons/thumb/1/11/Test-Logo.svg/783px-Test-Logo.svg.png"
            ));
        }

        return news;
    }
}