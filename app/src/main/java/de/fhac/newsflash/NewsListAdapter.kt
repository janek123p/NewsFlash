package de.fhac.newsflash

import android.R
import android.content.Context
import android.graphics.BitmapFactory
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import java.net.URL

class NewsListAdapter(
    private val context: Context,
    private val data: List<News>
) : BaseAdapter() {
    override fun getCount(): Int {
        return data.count()
    }

    override fun getItem(p0: Int): Any {
        return data[p0]
    }

    override fun getItemId(p0: Int): Long {
        return p0.toLong()
    }

    override fun getView(p0: Int, p1: View?, p2: ViewGroup?): View {
        val inflater = context
            .getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        var convertView = inflater.inflate(de.fhac.newsflash.R.layout.news_card, null)
        val news = data[p0]
        return convertView.apply {
            findViewById<ImageView>(de.fhac.newsflash.R.id.thumbnail).setImageResource(de.fhac.newsflash.R.drawable.rss_feed);
            findViewById<TextView>(de.fhac.newsflash.R.id.news_title)
                .setText(news.title)
            findViewById<TextView>(de.fhac.newsflash.R.id.news_content)
                .setText(news.content)
        }
    }
}