package de.fhac.newsflash.data.models

class Filter {

    val sources = mutableListOf<ISource>();
    val tags = mutableListOf<Tag>();


    fun add(source: ISource) : Filter{
        if(!sources.contains(source))
            sources.add(source);
        return this;
    }

    fun add(tag: Tag) : Filter{
        if(!tags.contains(tag))
            tags.add(tag);
        return this;
    }

}