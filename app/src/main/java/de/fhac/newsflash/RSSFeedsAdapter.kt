package de.fhac.newsflash

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import de.fhac.newsflash.data.controller.SourceController
import de.fhac.newsflash.data.models.ISource
import de.fhac.newsflash.data.models.RSSSource
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class RSSFeedsAdapter(private val settingsActivit: SettingsActivity) : BaseAdapter() {

    private var feeds: List<RSSSource> = mutableListOf()
    private var subscription = SourceController.getSourceStream().listen(this::updateFeeds, true)

    private fun updateFeeds(sources: List<ISource>?) {
        GlobalScope.launch {
            feeds = sources?.filterIsInstance<RSSSource>()?.toMutableList() ?: mutableListOf();
            settingsActivit.runOnUiThread { notifyDataSetChanged() }
        }
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
        val inflater =
            settingsActivit.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater

        var convertView = view ?: inflater.inflate(R.layout.rss_feed_card, null)

        var feed = feeds[position]

        convertView.findViewById<TextView>(R.id.txt_name).text = feed.getName()
        convertView.findViewById<TextView>(R.id.txt_link).text = feed.getUrl().toString()

        convertView.findViewById<Button>(R.id.bt_remove).setOnClickListener {
            SourceController.deleteSource(feed)
        }

        return convertView;
    }
}
