package eric.giangiulio.maintenanceinspectionapplication.Repository

import android.util.Log
import androidx.annotation.WorkerThread
import kotlinx.coroutines.flow.Flow

// Declares the DAO as a private property in the constructor. Pass in the DAO
// instead of the whole database, because you only need access to the DAO
class TaskRepository(private val taskDao: TaskDao) {

    // Room executes all queries on a separate thread.
    // Observed Flow will notify the observer when the data has changed.
    val allTasks: Flow<Map<Int,Task>> = taskDao.getAllTasks()

    // By default Room runs suspend queries off the main thread, therefore, we don't need to
    // implement anything else to ensure we're not doing long running database work
    // off the main thread.
    @WorkerThread
    suspend fun insert(task: Task) { // Add an insert method to insert tasks
        Log.d("TaskRepository", "Inserting task into database: ${task.title}, ID: ${task.id}")
        taskDao.insert(task)
    }

    @WorkerThread
    suspend fun update(task: Task) { // Add an update method to update tasks
        Log.d("TaskRepository", "Updating task in database: ${task.title}, ID: ${task.id}")
        taskDao.update(task)
        // need an update method
    }

    @WorkerThread
    suspend fun delete(task: Task) { // Add a delete method to delete tasks
        taskDao.delete(task)
    }

    @WorkerThread
    suspend fun getTaskById(taskId: Int): Task? { // Add a getTaskById method to get a task by its specific ID
        return taskDao.getTaskById(taskId)
    }
}