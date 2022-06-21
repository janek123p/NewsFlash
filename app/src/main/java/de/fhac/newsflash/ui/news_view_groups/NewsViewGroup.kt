package de.fhac.newsflash.ui.news_view_groups

import android.graphics.drawable.Drawable
import android.os.Build
import android.text.Html
import android.text.Spanned
import android.view.View
import android.widget.ImageView
import androidx.cardview.widget.CardView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import de.fhac.newsflash.R
import de.fhac.newsflash.ui.activities.MainActivity
import de.fhac.newsflash.data.models.News
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import org.jsoup.Jsoup
import org.jsoup.safety.Safelist
import java.util.*

abstract class NewsViewGroup(protected val mainActivity: MainActivity) {

    abstract fun getView(): View
    abstract fun getLatestPubDate(): Date

    companion object {

        suspend fun createViewGroups(
            data: List<News>,
            mainActivity: MainActivity
        ): MutableList<NewsViewGroup> {
            val result = GlobalScope.async {
                val list = mutableListOf<NewsViewGroup>()
                val indicesToDo = data.indices.toMutableList()

                list.addAll(extractGroups(data, indicesToDo, mainActivity))

                var numDouble = 0
                var numSingle = 0

                while (indicesToDo.size > 0) {
                    val probability =
                        if (numDouble + numSingle < 2) 0.3 else 0.6 * numSingle / (numDouble + numSingle)
                    if (indicesToDo.size >= 2 && Math.random() < probability) {
                        list.add(
                            DoubleNewsViewGroup(
                                data[indicesToDo.removeAt(0)],
                                data[indicesToDo.removeAt(0)],
                                mainActivity
                            )
                        )
                        ++numDouble
                    } else {
                        list.add(SingleNewsViewGroup(data[indicesToDo.removeAt(0)], mainActivity))
                        ++numSingle
                    }
                }

                list.sortByDescending { newsViewGroup -> newsViewGroup.getLatestPubDate() }

                return@async list
            }
            return result.await()
        }

        private fun extractGroups(
            data: List<News>,
            indicesToDo: MutableList<Int>,
            mainActivity: MainActivity,
            minGroupSize: Int = 4
        ): List<HorizontalScrollNewsViewGroup> {
            val viewGroups = mutableListOf<HorizontalScrollNewsViewGroup>()
            val indicesToRemove = mutableListOf<Int>()

            val sourceMap = data.groupBy { news -> news.source }
            for (source in sourceMap.keys) if (source != null) {
                val newsBySource = sourceMap[source]!!.sortedByDescending { news -> news.pubDate }

                var i = 1
                var sumDistance = 0
                var indexOfNews = data.indexOf(newsBySource[0])
                indicesToRemove += indexOfNews
                while (i < newsBySource.size) {
                    val newIndex = data.indexOf(newsBySource[i])
                    if (newIndex - indexOfNews <= 3 && sumDistance < 8) {
                        indicesToRemove += newIndex
                        sumDistance += (newIndex - indexOfNews - 1)
                        indexOfNews = newIndex
                        ++i
                    }
                    if (!(newIndex - indexOfNews <= 3 && sumDistance < 8) || i == newsBySource.size) {
                        if (indicesToRemove.size > minGroupSize) {
                            viewGroups += HorizontalScrollNewsViewGroup(
                                data.slice(indicesToRemove),
                                mainActivity.getString(R.string.news_from) + " " + source.getName(),
                                mainActivity
                            )
                            indicesToDo.removeAll(indicesToRemove)
                        }
                        sumDistance = 0
                        indicesToRemove.clear()
                        if (i + 1 < newsBySource.size) {
                            indexOfNews = data.indexOf(sourceMap[source]!![i + 1])
                        }
                        i += 2
                    }
                }
            }

            return viewGroups
        }

        fun cleanHTMLForTitle(title: String): String {
            return Jsoup.clean(title, Safelist.none())
        }

        fun cleanHTMLForContent(content: String): Spanned {
            return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) { //Show description with possible html tags, removes everything but text format
                Html.fromHtml(
                    Jsoup.clean(content, Safelist.basic()),
                    Html.FROM_HTML_MODE_COMPACT
                )
            } else {
                Html.fromHtml(Jsoup.clean(content, Safelist.basic()))
            }
        }

        fun loadImageAsynchronouslyIntoImageView(
            url: String,
            iv: ImageView,
            cv: CardView,
            mainActivity: MainActivity
        ) {
            Glide.with(mainActivity).load(url)
                .addListener(object : RequestListener<Drawable> {
                    override fun onLoadFailed(
                        e: GlideException?,
                        model: Any?,
                        target: Target<Drawable>?,
                        isFirstResource: Boolean
                    ): Boolean {
                        mainActivity.runOnUiThread {
                            cv.visibility = View.GONE
                        }
                        return true
                    }

                    override fun onResourceReady(
                        resource: Drawable?,
                        model: Any?,
                        target: Target<Drawable>?,
                        dataSource: DataSource?,
                        isFirstResource: Boolean
                    ): Boolean {
                        // to run this on ui thread leads to massive performance problems
                        // while scrolling through news list. As most of the time this works
                        // even when not called from ui thread we deliberately refrain
                        // from doing so. If a problem still occurs, we will run the code on
                        // UI thread
                        try {
                            iv.setImageDrawable(resource)
                        } catch (exc: Exception) {
                            mainActivity.runOnUiThread {
                                iv.setImageDrawable(resource)
                            }
                        }
                        return true
                    }
                }).submit()
        }
    }
}