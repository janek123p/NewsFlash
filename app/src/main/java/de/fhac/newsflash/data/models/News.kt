package de.fhac.newsflash.data.models

import java.net.URL

class News(val id: Int = (Math.random()*1000).toInt(), val name: String, val description: String, val url: String, val imageUrl : String? = null) {


}