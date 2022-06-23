package de.fhac.newsflash.data.repositories.models

import androidx.room.*
import de.fhac.newsflash.data.repositories.DateConverter
import java.util.*

@Entity(tableName = "news")
@TypeConverters(DateConverter::class)
class DatabaseNews(
    @PrimaryKey(autoGenerate = true) val uid: Long? = null,
    val title: String,
    val description: String,
    val url: String,
    val pubDate: Date,
    val imageUrl: String? = null,
    val favorite: Boolean = false,
    val sourceId: Long? = null
) {

}


