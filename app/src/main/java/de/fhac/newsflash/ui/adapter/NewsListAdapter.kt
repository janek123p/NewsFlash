package de.fhac.newsflash.ui.adapter

import android.view.View
import android.view.ViewGroup
import android.widget.*
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

    private var sub: StreamSubscription<List<News>> =
        NewsController.getNewsStream().listen(this::notify, true)

    private var viewGroups: List<NewsViewGroup>? = null


    fun launchReloadData(
        onFinished: Runnable? = null,
        filter: Filter? = null,
        filterFavourites: Boolean? = false
    ) {
        GlobalScope.launch {
            NewsController.refresh()
            onFinished?.run()
        }
    }

    fun pauseSubscriptions(){
        sub.pause()
    }

    fun resumeSubscriptions(){
        sub.resume()
    }

    private fun notify(newsList: List<News>?) {
        GlobalScope.launch {
            val data = newsList?.sortedByDescending { news -> news.pubDate } ?: mutableListOf()
            viewGroups = NewsViewGroup.createViewGroups(data, mainActivity)
            mainActivity.runOnUiThread {
                notifyDataSetChanged()
            }

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