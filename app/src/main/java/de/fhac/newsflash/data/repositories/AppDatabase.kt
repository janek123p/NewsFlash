package de.fhac.newsflash.data.repositories

import android.content.Context
import androidx.room.AutoMigration
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import de.fhac.newsflash.data.repositories.models.DatabaseNews
import de.fhac.newsflash.data.repositories.models.DatabaseSource

@Database(
    entities = [DatabaseSource::class, DatabaseNews::class],
    version = 2,
    autoMigrations = [ AutoMigration(from = 1, to = 2) ]
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun sourceRepository(): ISourceRepository
    abstract fun newsRepository(): INewsRepository


    /**
     * Copied from G4G
     */
    companion object {
        private var INSTANCE: AppDatabase? = null
        fun getDatabase(): AppDatabase? {
            return INSTANCE
        }

        fun initDatabase(context: Context): AppDatabase {
            if (INSTANCE == null) {
                synchronized(this) {
                    INSTANCE =
                        Room.databaseBuilder(context, AppDatabase::class.java, "news_database")
                            .build()
                }
            }
            return INSTANCE!!
        }
    }
}