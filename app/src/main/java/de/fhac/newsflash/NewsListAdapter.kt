package de.fhac.newsflash

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import com.bumptech.glide.Glide
import de.fhac.newsflash.data.controller.NewsController
import de.fhac.newsflash.data.models.News
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Runnable
import kotlinx.coroutines.launch

class NewsListAdapter(
    private val mainActivity: MainActivity
) : BaseAdapter() {

    private var data: List<News>? = null

    fun launchReloadData(refresh: Boolean, onFinished: Runnable? = null) {
        GlobalScope.launch {
            data =
                NewsController.getNews(refresh = refresh)
                    .sortedByDescending { news -> news.pubDate }
            mainActivity.runOnUiThread { notifyDataSetChanged() }
            onFinished?.run()
        }
    }

    override fun getCount(): Int {
        return data?.count() ?: 0
    }

    override fun getItem(pos: Int): Any {
        return data!![pos]
    }

    override fun getItemId(pos: Int): Long {
        return pos.toLong()
    }

    override fun getView(position: Int, view: View?, viewGroup: ViewGroup?): View {
        val inflater = mainActivity
            .getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater

        val convertView = view ?: inflater.inflate(R.layout.news_card, null)

        val news = data!![position]

        convertView.setOnClickListener {
            mainActivity.showDetailedNews(news)
        }

        return convertView.apply {
            val imgThumbnail = findViewById<ImageView>(R.id.thumbnail)
            if (news.imageUrl != null) {
                Glide.with(context).load(news.imageUrl).into(imgThumbnail)
                imgThumbnail.visibility = View.VISIBLE
            } else {
                imgThumbnail.visibility = View.GONE
            }

            findViewById<TextView>(R.id.news_title).text = news.name
            findViewById<TextView>(R.id.news_content).text = news.description
        }
    }
}