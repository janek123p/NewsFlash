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

/**
 * Adapter class to show all user defined RSS feeds
 */
class RSSFeedsAdapter(private val settingsActivity: SettingsActivity) : BaseAdapter() {

    private var feeds: List<RSSSource> = mutableListOf()
    private var subscription = SourceController.getSourceStream().listen(this::updateFeeds, true)

    /**
     * Update currently selected feeds
     */
    private fun updateFeeds(sources: List<ISource>?) {
        GlobalScope.launch {
            feeds = sources?.filterIsInstance<RSSSource>()?.toMutableList() ?: mutableListOf();
            settingsActivity.runOnUiThread { notifyDataSetChanged() }
        }
    }

    /**
     * return count of rss feeds
     */
    override fun getCount(): Int {
        return feeds.count()
    }

    /**
     * return item at specific position
     */
    override fun getItem(pos: Int): Any {
        return feeds[pos]
    }

    /**
     * return item it at specific position
     */
    override fun getItemId(pos: Int): Long {
        return pos.toLong()
    }

    /**
     * determine view of each element and add click listener for delete button
     */
    override fun getView(position: Int, view: View?, viewGroup: ViewGroup?): View {
        val inflater =
            settingsActivity.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater

        val convertView = view ?: inflater.inflate(R.layout.rss_feed_card, null)

        val feed = feeds[position]

        convertView.findViewById<TextView>(R.id.txt_name).text = feed.getName()
        convertView.findViewById<TextView>(R.id.txt_link).text = feed.getUrl().toString()

        convertView.findViewById<Button>(R.id.bt_remove).setOnClickListener {
            SourceController.deleteSource(feed) { exc ->
                Toast.makeText(
                    settingsActivity,
                    "Fehler beim Entfernen des RSS-Feeds : ${exc.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

        return convertView;
    }
}
