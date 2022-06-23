package de.fhac.newsflash.data.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import kotlinx.parcelize.RawValue
import java.net.URL

/**
 * Model representing a news source.
 *
 * Containing the name and url
 */
interface ISource : Parcelable {
    val id: Long

    fun getName() : String;
    fun getUrl() : String;
}