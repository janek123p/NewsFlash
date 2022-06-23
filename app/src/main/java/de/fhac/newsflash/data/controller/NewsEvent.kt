package de.fhac.newsflash.data.controller

import de.fhac.newsflash.data.models.News

/**
 * Events describing the news load states
 */
interface NewsEvent {
    /**
     * News are being read and parsed
     */
    class NewsLoadingEvent : NewsEvent;

    /**
     * News are available
     */
    class NewsLoadedEvent(val news: List<News>?) : NewsEvent;
}

