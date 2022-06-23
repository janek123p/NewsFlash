import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import de.fhac.newsflash.R
import de.fhac.newsflash.data.models.ISource
import de.fhac.newsflash.data.models.Tag
import de.fhac.newsflash.ui.activities.MainActivity
import de.fhac.newsflash.ui.filter.FilterHandler

class TagFilterAdapter(
    var tags: MutableList<Tag>,
    val mainActivity: MainActivity,
    private val filterHandler: FilterHandler
) : RecyclerView.Adapter<TagViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TagViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.filter_item, parent, false)

        return TagViewHolder(view, mainActivity)
    }

    override fun onBindViewHolder(holder: TagViewHolder, position: Int) {
        holder.setFilterItem(tags[position], selectFilterItem(tags[position]))
    }

    override fun getItemCount(): Int {
        return tags.count()
    }

    fun addFilterItem(tag: Tag) {
        this.tags.add(tag)
        notifyItemInserted(tags.count() - 1)
    }

    private fun selectFilterItem(tag: Tag): () -> Unit {
        return {
            if (tag in tags) {
                filterHandler.selectFilter(tag)
                removeFilterItem(tag)
            }
        }
    }

    private fun removeFilterItem(tag: Tag) {
        val itemIndex = tags.indexOf(tag)
        tags.removeAt(itemIndex)
        notifyItemRemoved(itemIndex)
    }
}

class TagViewHolder(val view: View, val mainActivity: MainActivity) :
    RecyclerView.ViewHolder(view) {
    private val filterText: TextView = view.findViewById(R.id.filter_item_text)

    fun setFilterItem(tag: Tag, onClick: () -> Unit) {
        filterText.text = tag.tagName
        view.setOnClickListener {
            onClick()
        }
    }
}