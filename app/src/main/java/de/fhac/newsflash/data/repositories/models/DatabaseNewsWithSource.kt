package de.fhac.newsflash.data.repositories.models

import androidx.room.Embedded
import androidx.room.Ignore
import androidx.room.Relation
import de.fhac.newsflash.data.models.News

class DatabaseNewsWithSource(
    @Embedded
    val news: DatabaseNews,
    @Relation(
        parentColumn = "sourceId",
        entityColumn = "uid"
    )
    var source: DatabaseSource? = null
) {
    @Ignore
    fun toNews(): News {
        return News(
            news.uid,
            news.title,
            news.description,
            news.url,
            news.pubDate,
            news.imageUrl,
            source?.toISource()
        )
    }
}