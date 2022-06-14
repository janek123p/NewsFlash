package de.fhac.newsflash.data.models

import java.net.URL

interface ISource {
    val id: Long

    fun getName() : String;
    fun getUrl() : String;
}