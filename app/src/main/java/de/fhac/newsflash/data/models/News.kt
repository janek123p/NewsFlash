package de.fhac.newsflash.data.models

import android.os.Parcelable
import de.fhac.newsflash.data.repositories.models.DatabaseNews
import de.fhac.newsflash.data.repositories.models.DatabaseSource
import kotlinx.parcelize.Parcelize
import java.util.*

/**
 * Model representing a news article.
 *
 * @param id Internal id of the news for database purpose
 * @param title Title of the article
 * @param description Description of the article
 * @param url Url to the website of the article
 * @param pubDate Publishing date of the article
 * @param imageUrl Url of the preview image of the article
 * @param source Source of the article
 */
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