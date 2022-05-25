package de.fhac.newsflash.data.models

import java.net.URL

interface ISource {
    val id: Int;

    fun getName() : String;
    fun getUrl() : URL;
}