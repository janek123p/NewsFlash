package de.fhac.newsflash.ui.news_view_groups

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import de.fhac.newsflash.ui.activities.MainActivity
import de.fhac.newsflash.R
import de.fhac.newsflash.data.models.News
import de.fhac.newsflash.ui.UIExtensions.Companion.setOnClickListenerWithAnimation
import java.util.*

/**
 * Class to represent multiple news elements
 * @param data list of news elements
 * @param heading Heading of this news group
 * @param mainActivity MainActivity
 */
class HorizontalScrollNewsViewGroup(
    private var data: List<News>,
    private var heading: String,
    mainActivity: MainActivity
) : NewsViewGroup(mainActivity) {

    override fun getView(view: View?): View {
        val inflater = mainActivity
            .getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater

        // inflate new View if view is null
        val convertView = view ?: inflater.inflate(R.layout.news_card_multiple, null)

        return convertView.apply {
            // Set Adapter and LayoutManager for recyclerview
            val recyclerView = findViewById<RecyclerView>(R.id.list_news)
            recyclerView.adapter =
                HorizontalNewsAdapter(data, mainActivity)
            recyclerView.layoutManager =
                LinearLayoutManager(mainActivity, RecyclerView.HORIZONTAL, false)

            // Set heading of group
            findViewById<TextView>(R.id.txt_group_heading).text = heading
        }
    }

    override fun getLatestPubDate(): Date {
        return data.maxOf { news -> news.pubDate }
    }

    override fun getType(): TYPE {
        return TYPE.MULTIPLE
    }
}

/**
 * Adapter to show multiple news elements in a horizontal manner
 * @param data list of News
 * @param mainActivity MainActivity
 */
class HorizontalNewsAdapter(
    private val data: List<News>,
    private val mainActivity: MainActivity
) :
    RecyclerView.Adapter<HorizontalNewsAdapter.ViewHolder>() {

    /**
     * ViewHolder class representing a single news element
     * @param view View held by this class
     * @param mainActivity MainActivity
     */
    class ViewHolder(private val view: View, private val mainActivity: MainActivity) :
        RecyclerView.ViewHolder(view) {

        private val imgThumbnail: ImageView = view.findViewById(R.id.img_thumbnail)
        private val imgCardView: CardView = view.findViewById(R.id.img_card_view)
        private val txtSource: TextView = view.findViewById(R.id.news_source)
        private val txtTitle: TextView = view.findViewById(R.id.news_title)
        private val txtContent: TextView = view.findViewById(R.id.news_content)

        // Bind news to corresponding View
        fun setNews(news: News) {
            // Add onClickListener
            view.setOnClickListenerWithAnimation {
                mainActivity.showDetailedNews(news)
            }

            // Possibly load image
            if (news.imageUrl != null) {
                NewsViewGroup.loadImageAsynchronouslyIntoImageView(
                    news.imageUrl,
                    imgThumbnail,
                    imgCardView,
                    mainActivity
                )
                imgCardView.visibility = View.VISIBLE
            } else {
                imgCardView.visibility = View.GONE
            }

            // Set title and brief content
            txtTitle.text =
                NewsViewGroup.cleanHTMLForTitle(news.title) //Remove possible HTML tags
            txtContent.text =
                NewsViewGroup.cleanHTMLForContent(news.description)

            // Set source if not null
            if (news.source != null) {
                txtSource.text = news.source!!.getName()
            } else {
                txtSource.visibility = View.GONE
            }
        }


    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.news_card_single_big, parent, false)

        return ViewHolder(view, mainActivity)
    }

    override fun getItemCount(): Int {
        return data.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.setNews(data[position])
    }

}