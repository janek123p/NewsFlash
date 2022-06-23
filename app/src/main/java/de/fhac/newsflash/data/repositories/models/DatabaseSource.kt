package de.fhac.newsflash.data.repositories.models

import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.Index
import androidx.room.PrimaryKey
import de.fhac.newsflash.data.models.ISource
import de.fhac.newsflash.data.models.RSSSource

@Entity(tableName = "source")
data class DatabaseSource(
    @PrimaryKey(autoGenerate = true) val uid: Long? = null,
    val name: String,
    val url: String
) {

    @Ignore
    fun toISource() : ISource{
        return RSSSource(uid!!, name, url);
    }
}