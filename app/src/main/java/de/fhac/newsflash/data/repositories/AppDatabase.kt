package de.fhac.newsflash.data.repositories

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import de.fhac.newsflash.data.repositories.models.DatabaseSource

@Database(entities = [DatabaseSource::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun sourceRepository() : ISourceRepository


    /**
     * Copied from G4G
     */
    companion object {
        private var INSTANCE: AppDatabase? = null
        fun getDatabase(context: Context): AppDatabase {
            if (INSTANCE == null) {
                synchronized(this) {
                    INSTANCE =
                        Room.databaseBuilder(context,AppDatabase::class.java, "news_database")
                            .build()
                }
            }
            return INSTANCE!!
        }
    }
}