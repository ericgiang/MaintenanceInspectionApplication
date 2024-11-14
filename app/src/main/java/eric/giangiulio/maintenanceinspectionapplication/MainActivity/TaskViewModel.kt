package eric.giangiulio.maintenanceinspectionapplication.MainActivity

import android.content.Context
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import eric.giangiulio.maintenanceinspectionapplication.Repository.Task
import eric.giangiulio.maintenanceinspectionapplication.Repository.TaskRepository
import eric.giangiulio.maintenanceinspectionapplication.Util.NotificationUtil
import kotlinx.coroutines.launch

class TaskViewModel(private val repository: TaskRepository) : ViewModel() {

    // Using LiveData and caching what allWords returns has several benefits:
    // - We can put an observer on the data (instead of polling for changes) and only update the
    //   the UI when the data actually changes.
    // - Repository is completely separated from the UI through the ViewModel.
    val allTasks: LiveData<Map<Int,Task>> = repository.allTasks.asLiveData() // LiveData object to hold all tasks

    /**
     * Launching a new coroutine to insert the data in a non-blocking way
     */

    fun insert(task: Task) = viewModelScope.launch {
        Log.d("TaskViewModel", "Inserting task with Title: ${task.title}, ID: ${task.id}")
        repository.insert(task) // Insert method to insert tasks
    }

    fun update(task: Task, context: Context) = viewModelScope.launch {
        Log.d("TaskViewModel", "Updating task with ID: ${task.id}, Title: ${task.title}")
        repository.update(task) // Update method to update tasks

        // If the task is completed, cancel the notification
        if (task.isCompleted) {
            NotificationUtil().cancelNotification(context, task)
        }
    }

    fun delete(task: Task) = viewModelScope.launch {
        repository.delete(task) // Delete method to delete tasks
    }
}

class TaskViewModelFactory(private val repository: TaskRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(TaskViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return TaskViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

