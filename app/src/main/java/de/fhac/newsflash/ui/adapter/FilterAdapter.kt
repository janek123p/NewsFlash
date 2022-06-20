package de.fhac.newsflash.ui.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import de.fhac.newsflash.R
import de.fhac.newsflash.data.models.Filter
import de.fhac.newsflash.ui.activities.MainActivity
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Runnable
import kotlinx.coroutines.launch

class FilterAdapter(
    private val mainActivity: MainActivity
) : BaseAdapter() {
    private var data: List<Filter>? = null

    fun launchReloadData(refresh: Boolean, onFinished: Runnable? = null) {
        GlobalScope.launch {
            //NewsController.refresh()
            //onFinished?.run()
        }
    }

    override fun getCount(): Int {
        return data?.count() ?: 0
    }

    override fun getItem(pos: Int): Any {
        return data!![pos]
    }

    override fun getItemId(pos: Int): Long {
        return pos.toLong()
    }

    override fun getView(position: Int, view: View?, viewGroup: ViewGroup?): View {
        val inflater = mainActivity
            .getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater

        val convertView = view ?: inflater.inflate(R.layout.filter_item, null)

        val filter = data!![position]

        return convertView.apply {
            findViewById<TextView>(R.id.filter_item).text = filter.sources[position].getName();
        }
    }
}