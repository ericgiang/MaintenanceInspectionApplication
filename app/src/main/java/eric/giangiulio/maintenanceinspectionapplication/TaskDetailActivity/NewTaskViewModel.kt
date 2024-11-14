package eric.giangiulio.maintenanceinspectionapplication.TaskDetailActivity

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import eric.giangiulio.maintenanceinspectionapplication.Repository.Task
import eric.giangiulio.maintenanceinspectionapplication.Repository.TaskRepository
import eric.giangiulio.maintenanceinspectionapplication.Util.NotificationUtil
import kotlinx.coroutines.launch

class NewTaskViewModel(private val repository: TaskRepository) : ViewModel() {

    private val _task = MutableLiveData<Task>().apply { value = null }
    val task: LiveData<Task> get() = _task

    fun start(taskId: Int) {
        viewModelScope.launch {
            _task.value = repository.getTaskById(taskId)
        }
    }

    fun insert(task: Task) = viewModelScope.launch {
        repository.insert(task)
    }

    fun update(task: Task, context: Context) = viewModelScope.launch {
        repository.update(task)
        // If the task is completed, cancel the notification
        if (task.isCompleted) {
            NotificationUtil().cancelNotification(context, task)
        }
    }
}

class NewTaskViewModelFactory(private val repository: TaskRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(NewTaskViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return NewTaskViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}