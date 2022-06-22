package de.fhac.newsflash.ui.adapter

import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import androidx.recyclerview.widget.RecyclerView
import de.fhac.newsflash.data.controller.NewsController
import de.fhac.newsflash.data.models.Filter
import de.fhac.newsflash.data.models.News
import de.fhac.newsflash.data.stream.StreamSubscription
import de.fhac.newsflash.ui.activities.MainActivity
import de.fhac.newsflash.ui.news_view_groups.NewsViewGroup
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Runnable
import kotlinx.coroutines.launch

class NewsListAdapter(
    private val mainActivity: MainActivity
) : BaseAdapter() {

    private var newsSub: StreamSubscription<List<News>> =
        NewsController.getNewsStream().listen(this::notifyNews, true)
    private var newsData: List<News> = listOf()

    private var favSub: StreamSubscription<List<News>> =
        NewsController.getFavoritesStream().listen(this::notifyFavorites, true)
    private var favData: List<News> = listOf()

    private var viewGroups: List<NewsViewGroup>? = null

    var filterFavorites: Boolean = false

    fun launchReloadData(
        onFinished: Runnable? = null,
        filter: Filter? = null,
    ) {
        GlobalScope.launch {
            NewsController.refresh(filter)
            val data: List<News> = if (filterFavorites) {
                favData
            } else {
                newsData
            }
            viewGroups = NewsViewGroup.createViewGroups(data, mainActivity)
            mainActivity.runOnUiThread {
                notifyDataSetChanged()
                onFinished?.run()
            }
        }
    }

    fun pauseSubscriptions() {
        favSub.pause()
        newsSub.pause()
    }

    fun resumeSubscriptions() {
        newsSub.resume()
        favSub.resume()
    }

    private fun notifyNews(newsList: List<News>?) {
        GlobalScope.launch {
            newsData = newsList?.sortedByDescending { news -> news.pubDate } ?: mutableListOf()
        }
    }

    private fun notifyFavorites(newsList: List<News>?) {
        GlobalScope.launch {
            favData = newsList?.sortedByDescending { news -> news.pubDate } ?: mutableListOf()
        }
    }

    override fun getCount(): Int {
        return viewGroups?.count() ?: 0
    }

    override fun getItem(pos: Int): Any {
        return viewGroups!![pos]
    }

    override fun getItemId(pos: Int): Long {
        return pos.toLong()
    }

    override fun getView(position: Int, view: View?, p2: ViewGroup?): View? {
        return viewGroups!![position].getView()
    }
}