package de.fhac.newsflash.ui.filter

import TagFilterAdapter
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import de.fhac.newsflash.R
import de.fhac.newsflash.data.controller.NewsController
import de.fhac.newsflash.data.controller.SourceController
import de.fhac.newsflash.data.models.Filter
import de.fhac.newsflash.data.models.ISource
import de.fhac.newsflash.data.models.Tag
import de.fhac.newsflash.ui.activities.MainActivity

class FilterHandler(val mainActivity: MainActivity) : Fragment() {
    private lateinit var selectedFilterAdapter: SelectedFilterAdapter
    private lateinit var sourceFilterAdapter: SourceFilterAdapter
    private lateinit var tagFilterAdapter: TagFilterAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val inflater = mainActivity
            .getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater

        val convertView = inflater.inflate(R.layout.filter_container, null)

        return convertView.apply {
            findViewById<ImageView>(R.id.filter_dropdown_button).setOnClickListener {
                val filters = findViewById<LinearLayout>(R.id.filters)
                if (filters.visibility == View.VISIBLE) {
                    filters.visibility = View.GONE
                } else if (filters.visibility == View.GONE) {
                    filters.visibility = View.VISIBLE
                }
            }
            val filter = Filter()
            val sources: MutableList<ISource>? = SourceController.getSourceStream().getLatest()
            val tags: MutableList<Tag> = mutableListOf(*Tag.values())
            val selectedFilterView = findViewById<RecyclerView>(R.id.selected_filter_container)
            selectedFilterView.adapter = SelectedFilterAdapter(filter, mainActivity, this@FilterHandler)
            selectedFilterView.layoutManager =
                LinearLayoutManager(mainActivity, RecyclerView.HORIZONTAL, false)
            selectedFilterAdapter = selectedFilterView.adapter as SelectedFilterAdapter
            val sourceFilterView = findViewById<RecyclerView>(R.id.source_filter_container)
            sourceFilterView.adapter = SourceFilterAdapter(sources, mainActivity, this@FilterHandler)
            sourceFilterView.layoutManager =
                LinearLayoutManager(mainActivity, RecyclerView.HORIZONTAL, false)
            sourceFilterAdapter = sourceFilterView.adapter as SourceFilterAdapter
            val tagFilterView = findViewById<RecyclerView>(R.id.tag_filter_container)
            tagFilterView.adapter = TagFilterAdapter(tags, mainActivity, this@FilterHandler)
            tagFilterView.layoutManager =
                LinearLayoutManager(mainActivity, RecyclerView.HORIZONTAL, false)
            tagFilterAdapter = tagFilterView.adapter as TagFilterAdapter
        }
    }

    fun selectFilter(source: ISource) {
        selectedFilterAdapter.addFilterItem(source)
    }

    fun selectFilter(tag: Tag) {
        selectedFilterAdapter.addFilterItem(tag)
    }

    fun removeFilter(source: ISource) {
        sourceFilterAdapter.addFilterItem(source)
    }

    fun removeFilter(tag: Tag) {
        tagFilterAdapter.addFilterItem(tag)
    }
}
