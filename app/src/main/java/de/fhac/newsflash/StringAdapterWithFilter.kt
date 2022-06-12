package de.fhac.newsflash

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*


class StringAdapterWithFilter(
    context: Context,
    resId: Int,
    private var suggestions: MutableList<String>
) : ArrayAdapter<String>(context, resId, suggestions), Filterable {

    private var allValues = suggestions.toList()

    override fun getFilter(): Filter {
        return object : Filter() {
            override fun performFiltering(constraint: CharSequence?): FilterResults {
                val results = FilterResults()
                if (constraint != null && constraint.isNotEmpty()) {
                    results.values = allValues.filter { item ->
                        item.contains(
                            constraint.toString(),
                            ignoreCase = true
                        )
                    }
                    results.count = (results.values as List<*>).size
                }else{
                    results.values = emptyList<String>()
                    results.count = 0
                }
                return results
            }

            override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
                val resultList = results?.values as? List<String>
                if (resultList != null) {
                    suggestions.clear()
                    suggestions.addAll(resultList)
                }
                notifyDataSetChanged()
            }

        }
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val inflater = context
            .getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater

        if(convertView == null) {
            var view = inflater.inflate(R.layout.dropdown_item, null)
            val txtLink = view.findViewById<TextView>(R.id.txt_link)
            txtLink.text = suggestions[position]
            return view
        }else{
            val txtLink = convertView.findViewById<TextView>(R.id.txt_link)
            txtLink.text = suggestions[position]
            return convertView
        }
    }

}