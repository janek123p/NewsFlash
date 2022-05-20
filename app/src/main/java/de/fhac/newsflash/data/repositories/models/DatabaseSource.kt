package de.fhac.newsflash.data.repositories.models

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "source")
data class DatabaseSource(
    @PrimaryKey(autoGenerate = true) val uid: Int,
    val name: String,
    val url: String
) {
}