package de.fhac.newsflash.data.repositories.models

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "source")
data class DatabaseSource(
    @PrimaryKey(autoGenerate = true) val uid: Long,
    val name: String,
    val url: String
) {
}