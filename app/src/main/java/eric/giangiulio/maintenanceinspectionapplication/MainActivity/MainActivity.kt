package eric.giangiulio.maintenanceinspectionapplication.MainActivity

import eric.giangiulio.maintenanceinspectionapplication.Util.NotificationUtil
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import eric.giangiulio.maintenanceinspectionapplication.R
import eric.giangiulio.maintenanceinspectionapplication.TaskDetailActivity.TaskDetailActivity
import eric.giangiulio.maintenanceinspectionapplication.TasksApplication
import eric.giangiulio.maintenanceinspectionapplication.Repository.Task // Add this import statement


class MainActivity : AppCompatActivity() {

    // ViewModel object to communicate between Activity and repository
    private val taskViewModel: TaskViewModel by viewModels {
        TaskViewModelFactory((application as TasksApplication).repository)
    }

    private var isCompletedTasksVisible = false // Variable to hold the visibility of completed tasks


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        // Notification channel
        NotificationUtil().createNotificationChannel(this)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // RecyclerView for active tasks
        val recyclerView = findViewById<RecyclerView>(R.id.recyclerview)
        // Adapter class, passing the launchNewTaskActivity callback
        val adapter = TaskListAdapter(
            this::launchNewTaskActivity,
            this::onDeleteClick,
            this::onCheckBoxClick
        )
        // Set the adapter for the recyclerView to the adapter object
        recyclerView.adapter = adapter
        // Set the recyclerView layout to be a linearLayoutManager with activity context
        recyclerView.layoutManager = LinearLayoutManager(this)

        // RecyclerView for completed tasks
        val completedRecyclerView = findViewById<RecyclerView>(R.id.completed_recyclerview)
        val completedAdapter = TaskListAdapter(
            this::launchNewTaskActivity,
            this::onDeleteClick,
            this::onCheckBoxClick,
            )
        completedRecyclerView.adapter = completedAdapter
        completedRecyclerView.layoutManager = LinearLayoutManager(this)

        // Start observing the tasks map, and pass updates through
        // to the adapter
        taskViewModel.allTasks.observe(this, Observer { tasks ->
            tasks?.let {
                adapter.submitList(it.values.filter { task -> !task.isCompleted })
                updateCompletedTasksVisibility(it.values.count { task -> task.isCompleted })
            } // Update the visibility of completed tasks
        })

        // Floating action button to add a new task
        val fab = findViewById<FloatingActionButton>(R.id.fab)
        // Start the TaskDetailActivity when it is clicked
        fab.setOnClickListener {
            // Launch a new task activity when the FAB is clicked
            launchNewTaskActivity(-1)
        }

        // Text view to show the number of completed tasks
        val completedTasksText = findViewById<TextView>(R.id.completed_tasks_text)
        completedTasksText.setOnClickListener {
            isCompletedTasksVisible = !isCompletedTasksVisible
            completedRecyclerView.visibility = if (isCompletedTasksVisible) View.VISIBLE else View.GONE
            taskViewModel.allTasks.value?.let { tasksMap ->
                val completedTasks = tasksMap.values.filter { it.isCompleted }
                if (isCompletedTasksVisible) {
                    completedTasksText.text = "Completed (${completedTasks.size})"
                    completedAdapter.submitList(completedTasks)
                } else {
                    completedTasksText.text = if (completedTasks.isNotEmpty()) "Completed (${completedTasks.size})" else "Completed"
                }
            }
        }
    }

    // Function to toggle visibility of completed tasks
    private fun onCheckBoxClick(task: Task) {
        // Toggle the completion status of the task
        task.isCompleted = !task.isCompleted
        Log.d("MainActivity", "Task ID: ${task.id}, isCompleted: ${task.isCompleted}")
        // Update the task in the ViewModel
        taskViewModel.update(task,this)

        // Cancel the notification if the task is marked as completed
        if (task.isCompleted) {
            NotificationUtil().cancelNotification(this, task) // 'this' refers to the MainActivity such that the notification is canceled in the context of the MainActivit
            Log.d("MainActivity", "Notification canceled for task ID: ${task.id}")
        }

        // Get the current list of tasks from the ViewModel
        taskViewModel.allTasks.value?.let { tasksMap ->
            val allTasks = tasksMap.values.toList()

            // Update the uncompleted tasks list
            val uncompletedTasks = allTasks.filter { !it.isCompleted }
            val uncompletedTasksAdapter = (findViewById<RecyclerView>(R.id.recyclerview).adapter as TaskListAdapter)
            uncompletedTasksAdapter.submitList(uncompletedTasks)

            // Update the completed tasks list
            if (isCompletedTasksVisible) {
                val completedTasks = allTasks.filter { it.isCompleted }
                val completedTasksAdapter = (findViewById<RecyclerView>(R.id.completed_recyclerview).adapter as TaskListAdapter)
                completedTasksAdapter.submitList(completedTasks)
            }

            // Update the visibility of completed tasks
            updateCompletedTasksVisibility(allTasks.count { it.isCompleted })
        }
    }

    /**
     *
     *
     **  Private helper functions for MainActivity Ui:
     *
     *
     **/

    // Private helper function that deletes a task in both the uncompleted and completed tasks sections
    private fun onDeleteClick(task: Task) {
        // Cancel any notifications for the task
        NotificationUtil().cancelNotification(this, task) // Delete the task from the ViewModel
        taskViewModel.delete(task)
        // Observe the changes in the task list
        taskViewModel.allTasks.observe(this, Observer { tasksMap ->
            // Variable to hold the active and completed tasks
            val activeTasks = tasksMap.values.filter { !it.isCompleted }
            val completedTasks = tasksMap.values.filter { it.isCompleted }
            // Filter the tasks to get only the uncompleted tasks
            val adapter = (findViewById<RecyclerView>(R.id.recyclerview).adapter as TaskListAdapter)
            // Submit the list of active tasks to the adapter
            adapter.submitList(activeTasks)

            // Filter the tasks to get only the completed tasks
            val completedTasksAdapter =
                (findViewById<RecyclerView>(R.id.completed_recyclerview).adapter as TaskListAdapter)
            // Submit the list of completed tasks to the adapter
            completedTasksAdapter.submitList(completedTasks)

            // Update the visibility and text of the completed tasks section
            updateCompletedTasksVisibility(completedTasks.size) // Update the visibility of completed tasks
        })
    }

    // Function to allow dynamic visibility of completed tasks
    private fun updateCompletedTasksVisibility(taskCount: Int) {
        val completedTasksText = findViewById<TextView>(R.id.completed_tasks_text)
        val separator = findViewById<View>(R.id.separator)
        // Variable to hold the text for the completed tasks with a conditional check
        // that will either show the number of completed tasks if there is at least one
        // completed task, or just "Completed"
        completedTasksText.text = if (taskCount > 0) "Completed ($taskCount)" else "Completed"
        // In all cases, the completed tasks text and separator will be visible
        // such that the user can still see the text "Completed" even if there are no
        // completed tasks
        if (taskCount > 0) {
            completedTasksText.visibility = View.VISIBLE
            separator.visibility = View.VISIBLE
        } else {
            completedTasksText.visibility = View.VISIBLE
            separator.visibility = View.VISIBLE
        }
    }

    // Function to launch the TaskDetailActivity with a task ID
    fun launchNewTaskActivity(id: Int) {
        val secondActivityIntent = Intent(this, TaskDetailActivity::class.java)
        secondActivityIntent.putExtra("TASK_ID", id)
        this.startActivity(secondActivityIntent)
    }
}