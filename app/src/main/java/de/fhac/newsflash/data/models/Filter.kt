package de.fhac.newsflash.data.models

class Filter {

    internal val sources = mutableListOf<ISource>();
    internal val tags = mutableListOf<Tag>();


    /**
     * Add a source to the filter
     */
    fun add(source: ISource) : Filter{
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