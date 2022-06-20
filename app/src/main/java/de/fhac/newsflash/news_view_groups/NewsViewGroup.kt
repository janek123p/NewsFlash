package de.fhac.newsflash.news_view_groups

import android.os.Build
import android.text.Html
import android.text.Spanned
import android.view.View
import de.fhac.newsflash.R
import de.fhac.newsflash.ui.activities.MainActivity
import de.fhac.newsflash.data.models.News
import org.jsoup.Jsoup
import org.jsoup.safety.Safelist
import java.util.*

abstract class NewsViewGroup(protected val mainActivity: MainActivity) {

    abstract fun getView(): View
    abstract fun getPubDate(): Date

    companion object {

        fun createViewGroups(
            data: List<News>,
            mainActivity: MainActivity
        ): MutableList<NewsViewGroup> {
            val list = mutableListOf<NewsViewGroup>()
            val indicesToDo = data.indices.toMutableList()

            list.addAll(extractGroups(data, indicesToDo, mainActivity))

            var numDouble = 0
            var numSingle = 0

            while (indicesToDo.size > 0) {
                var propability =
                    if (numDouble + numSingle < 2) 0.3 else 0.6 * numSingle / (numDouble + numSingle)
                if (indicesToDo.size >= 2 && Math.random() < 0.3) {
                    list.add(
                        DoubleNewsViewGroup(
                            data[indicesToDo.removeAt(0)],
                            data[indicesToDo.removeAt(0)],
                            mainActivity
                        )
                    )
                    numDouble++
                } else {
                    list.add(SingleNewsViewGroup(data[indicesToDo.removeAt(0)], mainActivity))
                    numSingle++
                }
            }

            list.sortByDescending { newsViewGroup -> newsViewGroup.getPubDate() }
            return list
        }

        private fun extractGroups(
            data: List<News>,
            indicesToDo: MutableList<Int>,
            mainActivity: MainActivity,
            groupSize: Int = 5
        ): List<HorizontalScrollNewsViewGroup> {
            var viewGroups = mutableListOf<HorizontalScrollNewsViewGroup>()

            var i = 0
            outer@ while (i < data.size - groupSize) {
                val source = data[i].source ?: continue@outer

                for (j in 1 until groupSize) {
                    if (source != data[i + j].source) {
                        i += j
                        continue@outer
                    }
                }

                var j = groupSize
                while (i + j < data.size && source == data[i + j].source) {
                    ++j
                }
                indicesToDo.removeAll(i until i + j)
                viewGroups.add(
                    HorizontalScrollNewsViewGroup(
                        data.subList(i, i + j),
                        mainActivity.getString(R.string.news_from) + " " + source.getName(),
                        mainActivity
                    )
                )
                i += j
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
    }
}