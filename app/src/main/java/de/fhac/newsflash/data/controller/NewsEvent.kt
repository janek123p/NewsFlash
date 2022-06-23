package de.fhac.newsflash.data.controller

import de.fhac.newsflash.data.models.News

interface NewsEvent {
    class NewsLoadingEvent : NewsEvent;
    class NewsLoadedEvent(val news: List<News>?) : NewsEvent;
}

