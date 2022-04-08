package de.fhac.newsflash

import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ListView

class MainActivity : AppCompatActivity() {
    lateinit var newsList: MutableList<News>
    lateinit var newsListAdapter: NewsListAdapter
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        newsList = mutableListOf()

        for (i in 1..10)
            newsList.add(News(i.toString(),i.toString(), Uri.parse("https://upload.wikimedia.org/wikipedia/commons/thumb/b/b6/Image_created_with_a_mobile_phone.png/800px-Image_created_with_a_mobile_phone.png")))
        newsListAdapter = NewsListAdapter(this, newsList)
        findViewById<ListView>(R.id.news_list).adapter = newsListAdapter
        newsListAdapter.notifyDataSetChanged()
    }
}