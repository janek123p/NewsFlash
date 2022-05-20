package de.fhac.newsflash.data.repositories

import androidx.room.*
import de.fhac.newsflash.data.repositories.models.DatabaseSource

@Dao
interface ISourceRepository {

    @Query("SELECT * FROM source")
    fun getAll() : List<DatabaseSource>

    @Insert(onConflict = OnConflictStrategy.ABORT)
    fun insertAll(vararg sources: DatabaseSource);

    @Delete
    fun delete(databaseSource: DatabaseSource);
}