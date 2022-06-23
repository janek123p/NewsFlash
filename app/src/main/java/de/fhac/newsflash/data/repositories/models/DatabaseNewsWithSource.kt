package de.fhac.newsflash.data.repositories.models

import androidx.room.Embedded
import androidx.room.Relation

class DatabaseNewsWithSource(
    @Embedded
    val news: DatabaseNews,
    @Relation(
        parentColumn = "sourceId",
        entityColumn = "uid"
    )
    var source: DatabaseSource? = null
) {
}