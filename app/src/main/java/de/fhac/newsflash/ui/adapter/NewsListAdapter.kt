package de.fhac.newsflash.ui.adapter

import android.view.View
import android.view.ViewGroup
import android.widget.*
import android.widget.BaseAdapter
import de.fhac.newsflash.data.controller.NewsController
import de.fhac.newsflash.data.models.Filter
import de.fhac.newsflash.data.models.News
import de.fhac.newsflash.data.stream.StreamSubscription
import de.fhac.newsflash.ui.activities.MainActivity
import de.fhac.newsflash.ui.news_view_groups.NewsViewGroup
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Runnable
import kotlinx.coroutines.launch

/**
 * Adapter to handle news
 * @param mainActivity MainActivity
 */
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

    /**
     * Launch task to reload data
     */
    fun launchReloadData(
        onFinished: Runnable? = null,
        filter: Filter? = null,
        filterFavourites: Boolean? = false
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

    /**
     * Pause news subscription
     */
    fun pauseSubscriptions() {
        favSub.pause()
        newsSub.pause()
    }

    /**
     * Resume news subscription
     */
    fun resumeSubscriptions() {
        newsSub.resume()
        favSub.resume()
    }

    /**
     * Function to be called, when new news data arrives
     * @param newsList new news data
     */
    private fun notifyNews(newsList: List<News>?) {
        GlobalScope.launch {
            newsData = newsList?.sortedByDescending { news -> news.pubDate } ?: mutableListOf()
            mainActivity.runOnUiThread {
                notifyDataSetChanged()
            }
        }
    }

    /**
     * Function to be called, when new favorite news data arrives
     * @param newsList new news data
     */
    private fun notifyFavorites(newsList: List<News>?) {
        GlobalScope.launch {
            favData = newsList?.sortedByDescending { news -> news.pubDate } ?: mutableListOf()
            mainActivity.runOnUiThread {
                notifyDataSetChanged()
            }
        }
    }

    override fun getViewTypeCount(): Int {
        return NewsViewGroup.TYPE.values().size
    }

    override fun getItemViewType(position: Int): Int {
        return viewGroups!![position].getType().getInt()
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

    override fun getView(position: Int, view: View?, viewGroup: ViewGroup?): View? {
        println(position)

        return if (view?.tag == getItemViewType(position)) {
            // No new layout inflation needed ==> pass view to getView-method
            viewGroups!![position].getView(view)
        } else {
            // new layout inflation needed
            val retView = viewGroups!![position].getView(null)
            retView.tag = getItemViewType(position)
            retView
        }
    }
}