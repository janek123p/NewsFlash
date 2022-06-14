package de.fhac.newsflash

import android.content.Context
import android.os.Build
import android.text.Html
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import com.bumptech.glide.Glide
import de.fhac.newsflash.data.controller.NewsController
import de.fhac.newsflash.data.models.News
import de.fhac.newsflash.data.stream.StreamSubscription
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Runnable
import kotlinx.coroutines.launch
import org.jsoup.Jsoup
import org.jsoup.safety.Safelist

class NewsListAdapter(
    private val mainActivity: MainActivity
) : BaseAdapter() {

    private var sub: StreamSubscription<List<News>> = NewsController.getNewsStream().listen(this::notify, true);

    private var data: List<News>? = null

    fun launchReloadData(refresh: Boolean, onFinished: Runnable? = null) {
        GlobalScope.launch {
            NewsController.refresh()
            onFinished?.run()
        }
    }

    private fun notify(newsList: List<News>?){
        GlobalScope.launch {
            data = newsList?.sortedByDescending { news -> news.pubDate } ?: mutableListOf();

            mainActivity.runOnUiThread{
                notifyDataSetChanged()
            }
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

            findViewById<TextView>(R.id.news_title).text = Jsoup.clean(news.title, Safelist.none()) //Remove possible HTML tags

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) { //Show description with possible html tags, removes everything but text format
                findViewById<TextView>(R.id.news_content).text = Html.fromHtml(Jsoup.clean(news.description, Safelist.basic()), Html.FROM_HTML_MODE_COMPACT)
            } else {
                findViewById<TextView>(R.id.news_content).text = Html.fromHtml(Jsoup.clean(news.description, Safelist.basic()))
            }
        }
    }
}