package de.fhac.newsflash.data.controller

import de.fhac.newsflash.data.models.ISource
import de.fhac.newsflash.data.models.News

class NewsController {

    fun getNews() : List<News> {
        var news = emptyList<News>();

        for(i in 0..10){
            news.plus(News("Nachricht $i", "https://google.com/", "Ich bin die Beschreibung der Nachricht $i".repeat(3)));
        }

        return news;
    }

    fun getNews(source: ISource) : List<News> {
        var news = emptyList<News>();

        for(i in 0..10){
            news.plus(News("Nachricht $i von ${source.getName()}", source.getUrl().toString(), "Ich bin die Beschreibung der Nachricht $i".repeat(3)));
        }

        return news;
    }
}