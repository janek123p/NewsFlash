package de.fhac.newsflash.data.models

import android.os.Parcelable
import de.fhac.newsflash.data.repositories.models.DatabaseNews
import de.fhac.newsflash.data.repositories.models.DatabaseSource
import kotlinx.parcelize.Parcelize
import java.util.*

@Parcelize
class News(
    val id: Long? = null,
    val title: String,
    val description: String,
    val url: String,
    val pubDate: Date,
    val imageUrl: String? = null,
    var source: ISource? = null
) : Parcelable{

    override fun equals(other: Any?): Boolean {
        if(other !is News) return false;
        if(super.equals(other)) return true;

        return url.equals(other.url, ignoreCase = true);
    }

    fun toDatabase(favorite: Boolean = false) : DatabaseNews {
        return  DatabaseNews(
            id,
            title,
            description,
            url,
            pubDate,
            imageUrl,
            favorite,
            source?.id
        )
    }
}