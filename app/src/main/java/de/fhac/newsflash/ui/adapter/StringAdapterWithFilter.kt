package de.fhac.newsflash.ui.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.recyclerview.widget.DiffUtil
import de.fhac.newsflash.R

/**
 * Adapter to handle list of strings and add a filter to the adapter for text autocompletion
 *
 * @param context Context
 * @param suggestions list of possible suggestions
 */
class StringAdapterWithFilter(
    private var context: Context,
    private var suggestions: MutableList<String>
) : BaseAdapter(), Filterable {

    private var allValues = suggestions.toList()
    private var filter = AutoCompleteFilter(this)

    override fun getFilter(): Filter {
        return filter
    }

    override fun getCount(): Int {
        return suggestions.size
    }

    override fun getItem(pos: Int): Any {
        return suggestions[pos]
    }

    override fun getItemId(pos: Int): Long {
        return pos.toLong()
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val inflater = context
            .getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater

        // inflate view if needed
        val view = convertView ?: inflater.inflate(R.layout.dropdown_item, null)
        return view.apply {
            // Set link
            findViewById<TextView>(R.id.txt_link).text = suggestions[position]
        }
    }

    /**
     * Filter class to perform auto completion filtering
     */
    class AutoCompleteFilter(private var adapter: StringAdapterWithFilter) : Filter() {

        override fun performFiltering(constraint: CharSequence?): FilterResults {
            val results = FilterResults()
            if (constraint != null && constraint.isNotEmpty()) {
                // all items that contain constraint
                results.values = adapter.allValues.filter { item ->
                    item.contains(
                        constraint.toString(),
                        ignoreCase = true
                    )
                }
                results.count = (results.values as List<*>).size
            } else {
                // return emtpy results
                results.values = emptyList<String>()
                results.count = 0
            }
            return results
        }

        override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
            // publish data to the adapter
            val resultList = results?.values as? List<String>
            if (resultList != null) {
                adapter.suggestions.clear()
                adapter.suggestions.addAll(resultList)
                adapter.notifyDataSetChanged()
            }
        }

    }

}