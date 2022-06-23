package de.fhac.newsflash.ui.adapter

import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import de.fhac.newsflash.R
import de.fhac.newsflash.data.controller.NewsController
import de.fhac.newsflash.data.controller.NewsEvent
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

    private var sub: StreamSubscription<NewsEvent> =
        NewsController.getNewsStream().listen(this::notify, true)

    private var favSub: StreamSubscription<List<News>> =
        NewsController.getFavoritesStream().listen(this::notifyFav, true)

    var favorites = false;

    private var viewGroups: List<NewsViewGroup>? = null

    /**
     * Launch task to reload data
     */
    fun launchReloadData(
        onFinished: Runnable? = null,
    ) {
        GlobalScope.launch {
            NewsController.refresh()
            onFinished?.run()
        }
    }

    /**
     * Pause news subscription
     */
    fun pauseSubscriptions() {
        sub.pause()
    }

    /**
     * Resume news subscription
     */
    fun resumeSubscriptions() {
        sub.resume()
    }

    /**
     * Function to be called, when new news data arrives
     * @param newsList new news data
     */
    private fun notify(event: NewsEvent?) {
        if (favorites) return
        GlobalScope.launch {
            if (event is NewsEvent.NewsLoadingEvent) {
                mainActivity.runOnUiThread {
                    val pullToRefresh: SwipeRefreshLayout =
                        mainActivity.findViewById(R.id.pullToRefresh);
                    pullToRefresh.isRefreshing = true
                }
            } else if (event is NewsEvent.NewsLoadedEvent) {
                val data =
                    event.news?.sortedByDescending { news -> news.pubDate } ?: mutableListOf()
                viewGroups = NewsViewGroup.createViewGroups(data, mainActivity)
                mainActivity.runOnUiThread {
                    notifyDataSetChanged()
                    val pullToRefresh: SwipeRefreshLayout =
                        mainActivity.findViewById(R.id.pullToRefresh);
                    pullToRefresh.isRefreshing = false
                    pullToRefresh.isEnabled = true
                }
            }
        }
    }

    private fun notifyFav(favoritesList: List<News>?) {
        if (!favorites) return
        GlobalScope.launch {
            val data = favoritesList?.sortedByDescending { news -> news.pubDate } ?: mutableListOf()
            viewGroups = NewsViewGroup.createViewGroups(data, mainActivity)
            mainActivity.runOnUiThread {
                notifyDataSetChanged()
                val pullToRefresh: SwipeRefreshLayout =
                    mainActivity.findViewById(R.id.pullToRefresh);
                pullToRefresh.isRefreshing = false
            }
        }
    }

    fun setFavorite(v: Boolean) {
        favorites = v
        if (!favorites) return
        GlobalScope.launch {
            val data = NewsController.getFavoritesStream().getLatest()?.sortedByDescending { news -> news.pubDate } ?: mutableListOf()
            viewGroups = NewsViewGroup.createViewGroups(data, mainActivity)
            mainActivity.runOnUiThread {
                notifyDataSetChanged()
                val pullToRefresh: SwipeRefreshLayout =
                    mainActivity.findViewById(R.id.pullToRefresh);
                pullToRefresh.isEnabled = false
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