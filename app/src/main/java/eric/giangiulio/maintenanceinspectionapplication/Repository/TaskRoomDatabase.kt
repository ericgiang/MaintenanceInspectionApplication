package eric.giangiulio.maintenanceinspectionapplication.Repository

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

// Annotates class to be a Room Database with a table (entity) of the Word class
@Database(entities = arrayOf(Task::class), version = 2, exportSchema = false)
abstract class TaskRoomDatabase : RoomDatabase() {
    abstract fun taskDao(): TaskDao

    companion object {
        // Singleton prevents multiple instances of database opening at the
        // same time.
        @Volatile
        private var INSTANCE: TaskRoomDatabase? = null

        fun getDatabase(context: Context, scope:CoroutineScope): TaskRoomDatabase {
            // if the INSTANCE is not null, then return it,
            // if it is, then create the database
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    TaskRoomDatabase::class.java,
                    "task_database"
                )
                    .fallbackToDestructiveMigration() // Allow destructive migrations to update the database
                    .addCallback(TaskDatabaseCallback(scope))
                    .build()
                INSTANCE = instance
                // return instance
                instance
            }
        }
    }
    private class TaskDatabaseCallback(
        private val scope: CoroutineScope
    ) : Callback() {

        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            INSTANCE?.let { database ->
                scope.launch {
                    populateDatabase(database.taskDao())
                }
            }
        }

        fun populateDatabase(taskDao: TaskDao) {
        }
    }
}