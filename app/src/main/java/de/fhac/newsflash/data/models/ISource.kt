package de.fhac.newsflash.data.models

import java.net.URL

interface ISource {
    fun getName() : String;
    fun getUrl() : URL;
}