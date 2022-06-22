package de.fhac.newsflash.ui.news_view_groups

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import de.fhac.newsflash.ui.activities.MainActivity
import de.fhac.newsflash.R
import de.fhac.newsflash.data.models.News
import java.util.*

/**
 * Class to represent two news elements
 *
 * @param data1 first news element
 * @param data2 second news element
 * @param mainActivity MainActivity
 */
class DoubleNewsViewGroup(
    private var data1: News,
    private var data2: News,
    mainActivity: MainActivity
) : NewsViewGroup(mainActivity) {


    override fun getView(view: View?): View {
        val inflater = mainActivity
            .getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater

        val convertView = view ?: inflater.inflate(R.layout.news_card_double, null)


        return convertView.apply {
            // Set Tistle
            findViewById<TextView>(R.id.news_title).text =
                cleanHTMLForTitle(data1.title) //Remove possible HTML tags
            findViewById<TextView>(R.id.news_title_2).text =
                cleanHTMLForTitle(data2.title) //Remove possible HTML tags

            // Set content
            findViewById<TextView>(R.id.news_content).text = cleanHTMLForContent(data1.description)
            findViewById<TextView>(R.id.news_content_2).text =
                cleanHTMLForContent(data2.description)

            // Set source if not null, else set visivility of TextView to GONE
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

            // Add OnClickListener for both news element to show news in detailed view
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

    override fun getLatestPubDate(): Date {
        return maxOf(data1.pubDate, data2.pubDate)
    }

    override fun getType(): TYPE {
        return TYPE.DOUBLE
    }
}