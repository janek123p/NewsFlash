package de.fhac.newsflash.data.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
class News(
    val id: Int = (Math.random() * 1000).toInt(),
    val name: String,
    val description: String,
    val url: String,
    val imageUrl: String? = null
) : Parcelable{

}