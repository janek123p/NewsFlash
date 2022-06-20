package de.fhac.newsflash.data.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import kotlinx.parcelize.RawValue
import java.net.URL

interface ISource : Parcelable {
    val id: Long

    fun getName() : String;
    fun getUrl() : String;
}