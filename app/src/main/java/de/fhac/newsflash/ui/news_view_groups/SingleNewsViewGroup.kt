package de.fhac.newsflash.ui.news_view_groups

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import de.fhac.newsflash.ui.activities.MainActivity
import de.fhac.newsflash.R
import de.fhac.newsflash.data.models.News
import java.util.*


class SingleNewsViewGroup(private var data: News, mainActivity: MainActivity) :
    NewsViewGroup(mainActivity) {


    override fun getView(): View {
        val inflater = mainActivity
            .getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater

        val convertView = inflater.inflate(R.layout.news_card_single, null)

        return convertView.apply {
            setOnClickListener {
                mainActivity.showDetailedNews(data)
            }

            val imgThumbnail = findViewById<ImageView>(R.id.img_thumbnail)
            val imgCardView = findViewById<CardView>(R.id.img_card_view)
            if (data.imageUrl != null) {
                loadImageAsynchronouslyIntoImageView(
                    data.imageUrl!!,
                    imgThumbnail,
                    imgCardView,
                    mainActivity
                )
                imgCardView.visibility = View.VISIBLE
            } else {
                imgCardView.visibility = View.GONE
            }

            val txtSource = findViewById<TextView>(R.id.news_source)
            if (data.source != null) {
                txtSource.text = data.source!!.getName()
            } else {
                txtSource.visibility = View.GONE
            }

            findViewById<TextView>(R.id.news_title).text =
                cleanHTMLForTitle(data.title) //Remove possible HTML tags

            findViewById<TextView>(R.id.news_content).text = cleanHTMLForContent(data.description)
        }
    }

    override fun getLatestPubDate(): Date {
        return data.pubDate
    }
}