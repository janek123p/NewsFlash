package de.fhac.newsflash.data.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

/**
 * Model representing a filter. Containing the sources and tags to filter by.
 */
@Parcelize
class Filter : Parcelable{

    internal val sources = mutableListOf<ISource>();
    internal val tags = mutableListOf<Tag>();

    fun copy() : Filter{
        var filter = Filter();
        filter.sources.addAll(sources)
        filter.tags.addAll(tags)
        return filter;
    }

    /**
     * Add a source to the filter
     */
    fun add(source: ISource) : Filter {
        if(!sources.contains(source))
            sources.add(source);
        return this;
    }

    /**
     * Add a tag to the filter
     */
    fun add(tag: Tag) : Filter{
        if(!tags.contains(tag))
            tags.add(tag);
        return this;
    }

}