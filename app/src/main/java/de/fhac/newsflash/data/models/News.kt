package de.fhac.newsflash.data.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import java.util.*

@Parcelize
class News(
    val title: String,
    val description: String,
    val url: String,
    val pubDate: Date,
    val imageUrl: String? = null,
    var source: ISource? = null
) : Parcelable{

}