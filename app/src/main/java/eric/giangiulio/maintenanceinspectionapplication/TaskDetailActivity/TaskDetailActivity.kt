package eric.giangiulio.maintenanceinspectionapplication.TaskDetailActivity

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Intent // Import the Intent class
import android.os.Bundle // Import the Bundle class
import android.text.TextUtils // Import the TextUtils class for text-related utility methods
import android.util.Log
import android.widget.Button // Import the Button class
import android.widget.CheckBox // Import the CheckBox class
import android.widget.EditText // Import the EditText class
import androidx.activity.enableEdgeToEdge // Import the enableEdgeToEdge function for edge-to-edge display
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat // Import the ViewCompat class for view compatibility methods
import androidx.core.view.WindowInsetsCompat // Import the WindowInsetsCompat class for window insets handling
import eric.giangiulio.maintenanceinspectionapplication.R
import eric.giangiulio.maintenanceinspectionapplication.Repository.Task
import eric.giangiulio.maintenanceinspectionapplication.TasksApplication
import java.util.Calendar
import androidx.activity.viewModels // Import the viewModels function for creating a ViewModel instance
import eric.giangiulio.maintenanceinspectionapplication.AlarmReceiver
import eric.giangiulio.maintenanceinspectionapplication.Util.DatePickerFragment
import eric.giangiulio.maintenanceinspectionapplication.Util.NotificationIdGenerator
import eric.giangiulio.maintenanceinspectionapplication.Util.TimePickerFragment
import java.text.DateFormat



class TaskDetailActivity : AppCompatActivity() { // Define the TaskDetailActivity class inheriting from AppCompatActivity
    private lateinit var editTaskTitleView: EditText
    private lateinit var editTaskContentView: EditText
    private lateinit var editTaskDueDateView: EditText
    private lateinit var taskCheckBox: CheckBox
    private lateinit var task: Task
    private var dueDate: Long = System.currentTimeMillis()
    val newTaskViewModel: NewTaskViewModel by viewModels {
        NewTaskViewModelFactory((application as TasksApplication).repository)
    }

    override fun onCreate(savedInstanceState: Bundle?) { // Override the onCreate method
        super.onCreate(savedInstanceState) // Call the superclass's onCreate method
        enableEdgeToEdge() // Enable edge-to-edge display
        setContentView(R.layout.activity_new_task) // Set the layout for this activity

        // Handle window insets for edge-to-edge display
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets -> // Set an OnApplyWindowInsetsListener on the main view
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars()) // Get the system bars insets
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom) // Set padding to the view based on system bars insets
            insets // Return the insets
        }

        // Declare UI elements
        editTaskTitleView = findViewById(R.id.edit_task_title)
        editTaskContentView = findViewById(R.id.edit_task_content)
        editTaskDueDateView = findViewById(R.id.edit_task_due_date)
        taskCheckBox = findViewById(R.id.checkBox)
        // Get the task ID from the intent extras
        val id = intent.getIntExtra("TASK_ID", -1)
        Log.d("TaskDetailActivity", "Received TASK_ID: $taskId")
        if (id == -1) { // Check if the task ID is -1 (indicating a new task)
            // If no task ID is provided, create a new task with default values
            task = Task(null, "", "", false, dueDate, NotificationIdGenerator.getNextId()) // Set a new task with default values in the ViewModel
        } else { // If a task ID is provided
            // If a task ID is provided, load the task from the ViewModel
            newTaskViewModel.start(id) // Start the ViewModel with the provided task ID
            newTaskViewModel.task.observe(this) { // Observe the task LiveData
                if (it != null) { // If the task is not null
                    setTask(it) // Set the task in the activity UI
                }
            }
        }
        // Set up the due date EditText click listener
        editTaskDueDateView.setOnClickListener {
            showDateTimePicker()
        }

        // Set up the save button click listener
        val buttonSave = findViewById<Button>(R.id.button_save) // Find and assign the save button
        buttonSave.setOnClickListener { // Set an OnClickListener on the save button
            saveTask() // Call the saveTask function
        }

        val buttonShare = findViewById<Button>(R.id.button_share)
        buttonShare.setOnClickListener {
            shareTask()
        }
    }

    /**
     *
     *
     ** Private helper functions for the TaskDetailActivity:
     *
     *
     **/

    // Function to set the notifications based on on the specific task id



    // Function to set the task in the activity UI
    private fun setTask(task: Task) { // Function to set the task in the activity UI
        this.task = task // Local task is set to the provided task
        editTaskTitleView.setText(task.title) // Set the task title in the title EditText
        editTaskContentView.setText(task.content) // Set the task content in the content EditText
        editTaskDueDateView.setText(DateFormat.getDateTimeInstance(DateFormat.DEFAULT, DateFormat.SHORT).format(task.dueDate)) // Set the task due date in the due date EditText
        taskCheckBox.isChecked = task.isCompleted // Set the task completion status in the checkbox
        dueDate = task.dueDate // Set the due date to the task due date
    }

    // Function to save the task details
    private fun saveTask() { // Function to save the task
        val title = editTaskTitleView.text.toString() // Get the task title from the title EditText
        val content = editTaskContentView.text.toString() // Get the task content from the content EditText
        val isCompleted = taskCheckBox.isChecked // Get the task completion status from the checkbox

        if (TextUtils.isEmpty(title) || TextUtils.isEmpty(content)) { // Check if the title or content is empty
            setResult(RESULT_CANCELED, Intent()) // Set the result to canceled
        } else { // If the title and content are not empty
            task.title = title // Set the task title
            task.content = content // Set the task content
            task.isCompleted = isCompleted // Set the task completion status
            task.dueDate = dueDate // Set the task due date

            if (task.id == null) { // Check if the task ID is null
                task.notificationId = NotificationIdGenerator.getNextId()
                newTaskViewModel.insert(task) // Insert the task in the ViewModel
            } else { // If the task ID is not null
                newTaskViewModel.update(task, this) // Update the task in the ViewModel
            }
            setNotification(task)
            setResult(RESULT_OK) // Set the result to OK
        }
        finish() // Close TaskDetailActivity
    }
    private fun setNotification(task: Task) {
        val alarmManager = getSystemService(ALARM_SERVICE) as? AlarmManager
        val alarmIntent = Intent(applicationContext, AlarmReceiver::class.java).apply {
            putExtra("TASK_ID", task.id)
            putExtra("TASK_TITLE", task.title)
            putExtra("TASK_CONTENT", task.content)
        }
        // Log the current time and the task's due date
        val currentTime = Calendar.getInstance().timeInMillis
        Log.d("NotificationUtil", "Current time: $currentTime")
        Log.d("NotificationUtil", "Task due date: ${task.dueDate}")

        val pendingIntent = PendingIntent.getBroadcast(applicationContext, task.notificationId, alarmIntent, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)
        // Set the alarm to wake up the device and trigger the notification when the time is reached
        alarmManager?.setWindow(AlarmManager.RTC_WAKEUP, task.dueDate, 1000 * 10, pendingIntent)
        Log.d("NotificationUtil", "Notification scheduled with ID: ${task.notificationId} at time: ${task.dueDate}")
    }


    // Function to show dialog for date and time picker
    private fun showDateTimePicker() {
        val calendar = Calendar.getInstance()
        // Create and show a DatePickerDialog
        val datePickerFragment = DatePickerFragment { cal ->
            calendar.set(Calendar.YEAR, cal.get(Calendar.YEAR)) // Set the year in the calendar
            calendar.set(Calendar.MONTH, cal.get(Calendar.MONTH)) // Set the month in the calendar
            calendar.set(Calendar.DAY_OF_MONTH, cal.get(Calendar.DAY_OF_MONTH)) // Set the day of the month in the calendar

            // Create and show a TimePickerFragment
            val timePickerFragment = TimePickerFragment(calendar) { cal ->
                // Set the due date to the calendar time in milliseconds
                dueDate = cal.timeInMillis
                // Format the date and time using the default date and time formats in the editTaskDueDateView
                editTaskDueDateView.setText(DateFormat.getDateTimeInstance(DateFormat.DEFAULT, DateFormat.SHORT).format(cal.timeInMillis))
            }
            timePickerFragment.show(supportFragmentManager, "timePicker") // Show the TimePickerFragment with the supportFragmentManager
        }
        datePickerFragment.show(supportFragmentManager, "datePicker") // Show the DatePickerFragment with the supportFragmentManager
        }


    // Function to share the task details
    private fun shareTask() {
        val shareIntent = Intent().apply {
            action = Intent.ACTION_SEND
            // Set the task title and content in the share intent
            putExtra(Intent.EXTRA_TEXT, "${editTaskTitleView.text}\n${editTaskContentView.text}")
            type = "text/plain"
        }
        // Start the share intent with a chooser
        startActivity(Intent.createChooser(shareIntent, resources.getText(R.string.share)))
    }
}