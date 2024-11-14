package eric.giangiulio.maintenanceinspectionapplication.Repository

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.MapInfo
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface TaskDao { // Changed to TaskDao to handle Task objects
    @Suppress("DEPRECATION") // Suppress deprecation warning1
    @MapInfo(keyColumn = "id")
    @Query("SELECT * FROM task_table ORDER BY dueDate ASC")
    fun getAllTasks(): Flow<Map<Int,Task>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(task: Task)

    @Update
    suspend fun update(task: Task) // Update method to update tasks

    @Delete
    suspend fun delete(task: Task) // Delete method to delete tasks

    @Query("SELECT * FROM task_table WHERE id = :taskId LIMIT 1") // This will return a task with a specific ID
    suspend fun getTaskById(taskId: Int): Task? // Add a getTaskById method to get a task by its specific ID
}
