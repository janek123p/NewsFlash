package de.fhac.newsflash.ui.filter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import de.fhac.newsflash.R
import de.fhac.newsflash.data.controller.NewsController
import de.fhac.newsflash.data.models.Filter
import de.fhac.newsflash.data.models.ISource
import de.fhac.newsflash.data.models.Tag
import de.fhac.newsflash.ui.activities.MainActivity

class SelectedFilterAdapter(
    var filter: Filter,
    val mainActivity: MainActivity,
    private val filterHandler: FilterHandler
) : RecyclerView.Adapter<FilterViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FilterViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.filter_selected_item, parent, false)
        return FilterViewHolder(view, mainActivity)
    }

    override fun onBindViewHolder(holder: FilterViewHolder, position: Int) {
        val sourceCount = filter.sources.count()
        if (position >= sourceCount) {
            val tag = filter.tags[position - sourceCount]
            holder.setFilterItem(tag, removeFilterItem(tag))
        } else {
            val source = filter.sources[position]
            holder.setFilterItem(
                source,
                removeFilterItem(source)
            )
        }
    }

    override fun getItemCount(): Int {
        return filter.sources.count() + filter.tags.count()
    }

    fun addFilterItem(source: ISource) {
        this.filter.add(source)
        notifyItemInserted(filter.sources.indexOf(source))
        NewsController.setFilter(filter)
    }

    fun addFilterItem(tag: Tag) {
        this.filter.add(tag)
        notifyItemInserted(filter.tags.indexOf(tag) + filter.sources.count())
        NewsController.setFilter(filter)
    }

    private fun removeFilterItem(source: ISource): () -> Unit {
        return {
            val itemIndex = filter.sources.indexOf(source)
            filterHandler.removeFilter(source)
            this.filter.sources.remove(source)
            notifyItemRemoved(itemIndex)
        }
    }

    private fun removeFilterItem(tag: Tag): () -> Unit {
        return {
            val itemIndex = filter.tags.indexOf(tag)
            filterHandler.removeFilter(tag)
            this.filter.tags.remove(tag)
            notifyItemRemoved(itemIndex + filter.sources.count())
        }
    }
}

class FilterViewHolder(val view: View, val mainActivity: MainActivity) :
    RecyclerView.ViewHolder(view) {
    private val filterText: TextView = view.findViewById(R.id.selected_filter_item_text)

    fun setFilterItem(source: ISource, onClick: () -> Unit) {
        filterText.text = source.getName()
        view.setOnClickListener {
            onClick()
        }
    }

    fun setFilterItem(tag: Tag, onClick: () -> Unit) {
        filterText.text = tag.tagName
        view.setOnClickListener {
            onClick()
        }
    }
}
