package de.fhac.newsflash.data.repositories

import androidx.room.*
import de.fhac.newsflash.data.repositories.models.DatabaseNews
import de.fhac.newsflash.data.repositories.models.DatabaseNewsWithSource
import de.fhac.newsflash.data.repositories.models.DatabaseSource

@Dao
interface INewsRepository {

    @Transaction
    @Query("SELECT * FROM news")
    fun getAll() : List<DatabaseNewsWithSource>

    @Transaction
    @Query("SELECT * FROM news WHERE favorite=1")
    fun getAllFavorites() : List<DatabaseNewsWithSource>

    @Query("DELETE FROM news WHERE favorite=0")
    fun deleteAllNonFavorites();

    @Insert(onConflict = OnConflictStrategy.ABORT)
    fun insertAll(vararg news: DatabaseNews): List<Long>;

    @Insert(onConflict = OnConflictStrategy.ABORT)
    fun insert(news: DatabaseNews): Long;

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insertIgnore(news: DatabaseNews): Long;

    @Update
    fun update(news: DatabaseNews)

    @Transaction
    fun insertOrUpdate(news: DatabaseNews){
        if(insertIgnore(news) == -1L){
            update(news);
        }
    }

    @Delete
    fun delete(databaseNews: DatabaseNews);

}