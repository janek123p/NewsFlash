package de.fhac.newsflash.ui.filter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import de.fhac.newsflash.R
import de.fhac.newsflash.data.models.ISource
import de.fhac.newsflash.ui.activities.MainActivity

class SourceFilterAdapter(
    var sources: MutableList<ISource>?,
    val mainActivity: MainActivity,
    private val filterHandler: FilterHandler
) :
    RecyclerView.Adapter<SourceViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SourceViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.filter_item, parent, false)
        return SourceViewHolder(view, mainActivity)
    }

    override fun getItemCount(): Int {
        return sources?.count() ?: 0
    }

    override fun onBindViewHolder(holder: SourceViewHolder, position: Int) {
        holder.setFilterItem(sources!![position], selectFilterItem(sources!![position]))
    }

    fun addFilterItem(source: ISource) {
        this.sources!!.add(source)
        notifyItemInserted(sources!!.count() - 1)
    }

    private fun selectFilterItem(source: ISource): () -> Unit {
        return {
            if (sources == null || source in sources!!) {
                filterHandler.selectFilter(source)
                removeFilterItem(source)
            }
        }
    }

    private fun removeFilterItem(source: ISource) {
        val itemIndex = sources!!.indexOf(source)
        sources!!.removeAt(itemIndex)
        notifyItemRemoved(itemIndex)
    }
}

class SourceViewHolder(val view: View, val mainActivity: MainActivity) :
    RecyclerView.ViewHolder(view) {
    private val filterText: TextView = view.findViewById(R.id.filter_item_text)

    fun setFilterItem(source: ISource, onClick: () -> Unit) {
        filterText.text = source.getName()
        view.setOnClickListener {
            onClick()
        }
    }
}
