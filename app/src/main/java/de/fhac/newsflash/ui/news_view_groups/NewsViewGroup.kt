package de.fhac.newsflash.ui.news_view_groups

import android.graphics.drawable.Drawable
import android.os.Build
import android.text.Html
import android.text.Spanned
import android.view.View
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import de.fhac.newsflash.R
import de.fhac.newsflash.data.models.News
import de.fhac.newsflash.ui.activities.MainActivity
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import org.jsoup.Jsoup
import com.bumptech.glide.request.target.Target
import org.jsoup.safety.Safelist
import java.util.*


/**
 * abstract class NewsViewGroup to define functionality of a NewsViewGroup
 * NewsViewGroup represents a set of news. Depending on the type of NewsViewGroup
 * either a single news, two news or any amount of news can be represented by
 * the NewsViewGroup
 */
abstract class NewsViewGroup(protected val mainActivity: MainActivity) {

    /**
     * Function to get view for specific news element(s)
     * @param view View to use, if null a new view should be inflated
     * @return View showing the news item(s)
     */
    abstract fun getView(view: View? = null): View

    /**
     * Function to obtain the latest publishing date of all news handled by the NewsViewGroup
     * @return latest publishing date
     */
    abstract fun getLatestPubDate(): Date

    /**
     * Function to return NewsViewGroupType
     * @return type
     */
    abstract fun getType(): TYPE

    /**
     * enum class to represent type of NewsViewGroup
     */
    enum class TYPE(private val typeAsInt: Int) {
        SINGLE(0), DOUBLE(1), MULTIPLE(2);

        fun getInt(): Int {
            return typeAsInt
        }
    }

    companion object {

        /**
         * Function to create ViewGroups out of a list of news
         *
         * @param data list of news to be represented in the NewsViewGroups
         * @param mainActivity MainActivity
         */
        suspend fun createViewGroups(
            data: List<News>,
            mainActivity: MainActivity
        ): MutableList<NewsViewGroup> {
            //async as this process may take some time especially with many news
            val result = GlobalScope.async {
                val list = mutableListOf<NewsViewGroup>()
                val indicesToDo = data.indices.toMutableList()

                // Extract all groups (HorizontalScrollNewsViewGroups) and add to list
                // of NewsViewGroups
                list.addAll(extractGroups(data, indicesToDo, mainActivity))

                var numDouble = 0
                var numSingle = 0

                while (indicesToDo.size > 0) {
                    // calculate probability for choosing DoubleViewGroup
                    val probability =
                        if (numDouble + numSingle < 2) 0.15 else 0.4 * numSingle / (numDouble + numSingle)

                    if (indicesToDo.size >= 2 && (Math.random() < probability ||
                                (data[indicesToDo[0]].imageUrl == null && data[indicesToDo[1]].imageUrl == null))
                    ) {
                        // Add DoubleNewsViewGroup
                        list.add(
                            DoubleNewsViewGroup(
                                data[indicesToDo.removeAt(0)],
                                data[indicesToDo.removeAt(0)],
                                mainActivity
                            )
                        )
                        ++numDouble
                    } else {
                        // Add SingleNewsViewGroup
                        list.add(SingleNewsViewGroup(data[indicesToDo.removeAt(0)], mainActivity))
                        ++numSingle
                    }
                }

                list.sortByDescending { newsViewGroup -> newsViewGroup.getLatestPubDate() }

                return@async list
            }
            return result.await()
        }

        /**
         * Function to extract groups from list of news. Groups of at least minGroupSize news
         * from one source that occur in a temporal proximity will be extracted as a group
         *
         * @param data list of news
         * @param indicesToDo List of indices that still must be mapped to a NewsViewGroup
         * @param mainActivity MainActivity
         * @param minGroupSize Minimal size a group of news must have to be accounted
         * @param maxSumDistance Maximal sum of distances between the news of a group
         * @param maxDistance Maximal distance between two news of a group
         */
        private fun extractGroups(
            data: List<News>,
            indicesToDo: MutableList<Int>,
            mainActivity: MainActivity,
            minGroupSize: Int = 4,
            maxSumDistance: Int = 12,
            maxDistance: Int = 4
        ): List<HorizontalScrollNewsViewGroup> {
            val viewGroups = mutableListOf<HorizontalScrollNewsViewGroup>()
            val indicesToRemove = mutableListOf<Int>()

            // Group data by source
            val sourceMap = data.groupBy { news -> news.source }

            // For every source look for temporal proximity
            for (source in sourceMap.keys) if (source != null) {
                // Sort news by publishing date
                val newsBySource = sourceMap[source]!!.sortedByDescending { news -> news.pubDate }

                var i = 1
                var sumDistance = 0
                var indexOfNews = data.indexOf(newsBySource[0])
                indicesToRemove += indexOfNews
                // Loop over all news
                while (i < newsBySource.size) {
                    // Get index of news in data (corresponds to publishing date)
                    val newIndex = data.indexOf(newsBySource[i])
                    // If temporal proximity is given (difference of indices in data is less or
                    // equal to maxDistance and summed distance of the current group of
                    // news is less or equal to maxSumDistance) add this news to the group
                    if (newIndex - indexOfNews <= maxDistance && sumDistance <= maxSumDistance) {
                        indicesToRemove += newIndex
                        sumDistance += (newIndex - indexOfNews - 1)
                        indexOfNews = newIndex
                        ++i
                    }
                    // if the above stated conditions are not fulfilled or last news item is reached
                    // close group if it has at least minGroupSize items
                    if (!(newIndex - indexOfNews <= maxDistance && sumDistance <= maxSumDistance)
                        || i == newsBySource.size
                    ) {
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

        /**
         * Function to remove all HTML tags from title
         * @param title Title to remove html tags from
         */
        fun cleanHTMLForTitle(title: String): String {
            return Jsoup.clean(title, Safelist.none())
        }

        /**
         * Function to remove some HTML tags (see Safelist.basic())
         * @param content Content to remove HTML tags from
         */
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

        /**
         * Function to load image from url into ImageView asynchronously
         * @param url URL of the image
         * @param iv ImageView to load image into
         * @param surroundingView View to set visibility to false if loading of the image failed
         * @param mainActivity MainActivity
         */
        fun loadImageAsynchronouslyIntoImageView(
            url: String,
            iv: ImageView,
            surroundingView: View,
            mainActivity: MainActivity
        ) {
            // Submit loading URL
            Glide.with(mainActivity).load(url)
                .addListener(object : RequestListener<Drawable> {
                    override fun onLoadFailed(
                        e: GlideException?,
                        model: Any?,
                        target: Target<Drawable>?,
                        isFirstResource: Boolean
                    ): Boolean {
                        // If loading failed: set visibility of surrounding view to GONE
                        mainActivity.runOnUiThread {
                            surroundingView.visibility = View.GONE
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
                        // If loading URL has worked load image resource into ImageView
                        mainActivity.runOnUiThread {
                            if (!mainActivity.isDestroyed)
                                Glide.with(mainActivity).load(resource).into(iv)
                        }
                        return true
                    }
                }).submit()
        }


    }
}