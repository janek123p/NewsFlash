package de.fhac.newsflash.news_view_groups

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import de.fhac.newsflash.ui.activities.MainActivity
import de.fhac.newsflash.R
import de.fhac.newsflash.data.models.News
import java.util.*

class DoubleNewsViewGroup(
    private var data1: News,
    private var data2: News,
    mainActivity: MainActivity
) : NewsViewGroup(mainActivity) {


    override fun getView(): View {
        val inflater = mainActivity
            .getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater

        val convertView = inflater.inflate(R.layout.news_card_double, null)


        return convertView.apply {
            findViewById<TextView>(R.id.news_title).text =
                cleanHTMLForTitle(data1.title) //Remove possible HTML tags

            findViewById<TextView>(R.id.news_title_2).text =
                cleanHTMLForTitle(data2.title) //Remove possible HTML tags

            findViewById<TextView>(R.id.news_content).text = cleanHTMLForContent(data1.description)
            findViewById<TextView>(R.id.news_content_2).text =
                cleanHTMLForContent(data2.description)

            val txtSource1 = findViewById<TextView>(R.id.news_source)
            val txtSource2 = findViewById<TextView>(R.id.news_source_2)
            if (data1.source != null) {
                txtSource1.text = data1.source!!.getName()
            } else {
                txtSource1.visibility = View.GONE
            }
            if (data2.source != null) {
                txtSource2.text = data2.source!!.getName()
            } else {
                txtSource2.visibility = View.GONE
            }

            findViewById<LinearLayout>(R.id.lin_layout_news).setOnClickListener {
                mainActivity.showDetailedNews(
                    data1
                )
            }
            findViewById<LinearLayout>(R.id.lin_layout_news_2).setOnClickListener {
                mainActivity.showDetailedNews(
                    data2
                )
            }
        }
    }

    override fun getPubDate(): Date {
        return minOf(data1.pubDate, data2.pubDate)
    }
}