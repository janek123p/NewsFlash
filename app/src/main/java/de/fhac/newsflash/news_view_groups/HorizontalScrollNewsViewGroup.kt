package de.fhac.newsflash.news_view_groups

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.cardview.widget.CardView
import com.bumptech.glide.Glide
import de.fhac.newsflash.ui.activities.MainActivity
import de.fhac.newsflash.R
import de.fhac.newsflash.data.models.News
import java.util.*


class HorizontalScrollNewsViewGroup(
    private var data: List<News>,
    private var heading: String,
    mainActivity: MainActivity
) :
    NewsViewGroup(mainActivity) {

    override fun getView(): View {

        val inflater = mainActivity
            .getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater

        val convertView = inflater.inflate(R.layout.news_card_multiple, null)

        val linLayout = convertView.findViewById<LinearLayout>(R.id.lin_layout_news)
        convertView.findViewById<TextView>(R.id.txt_group_heading).text = heading

        for (i in data.indices) {
            val news = data[i]

            val child = View.inflate(mainActivity, R.layout.news_card_single_big, null)

            child.setOnClickListener {
                mainActivity.showDetailedNews(news)
            }

            val imgThumbnail = child.findViewById<ImageView>(R.id.img_thumbnail)
            val imgCardView = child.findViewById<CardView>(R.id.img_card_view)
            if (news.imageUrl != null) {
                Glide.with(mainActivity).load(news.imageUrl).into(imgThumbnail)
                imgCardView.visibility = View.VISIBLE
            } else {
                imgCardView.visibility = View.GONE
            }

            child.findViewById<TextView>(R.id.news_title).text =
                cleanHTMLForTitle(news.title) //Remove possible HTML tags

            child.findViewById<TextView>(R.id.news_content).text =
                cleanHTMLForContent(news.description)

            val txtSource = child.findViewById<TextView>(R.id.news_source)
            if (news.source != null) {
                txtSource.text = news.source!!.getName()
            } else {
                txtSource.visibility = View.GONE
            }

            linLayout.addView(child, i)
        }

        return convertView
    }

    override fun getPubDate(): Date {
        return data.minOf { news -> news.pubDate }
    }
}