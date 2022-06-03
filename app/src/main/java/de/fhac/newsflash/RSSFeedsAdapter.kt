package de.fhac.newsflash

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import de.fhac.newsflash.data.controller.NewsController
import de.fhac.newsflash.data.controller.SourceController
import de.fhac.newsflash.data.models.RSSSource

class RSSFeedsAdapterAdapter(private val context: Context) : BaseAdapter() {

    private lateinit var feeds: List<RSSSource>

    init {
        updateFeeds()
    }

    private fun updateFeeds() {
        feeds = SourceController.getSources().filterIsInstance<RSSSource>().toMutableList()
        notifyDataSetChanged()
    }

    override fun getCount(): Int {
        return feeds.count()
    }

    override fun getItem(pos: Int): Any {
        return feeds[pos]
    }

    override fun getItemId(pos: Int): Long {
        return pos.toLong()
    }

    override fun getView(position: Int, view: View?, viewGroup: ViewGroup?): View {
        val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        var convertView = inflater.inflate(R.layout.rss_feed_card, null)

        var feed = feeds[position]

        convertView.findViewById<TextView>(R.id.txt_name).text = feed.getName()
        convertView.findViewById<TextView>(R.id.txt_link).text = feed.getUrl().toString()

        convertView.findViewById<Button>(R.id.bt_remove).setOnClickListener {
            SourceController.deleteSource(feed.id)
            updateFeeds()
        }

        return convertView;
    }
}
