package eric.giangiulio.maintenanceinspectionapplication.MainActivity

import android.graphics.Paint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.CheckBox
import androidx.appcompat.widget.AppCompatImageButton
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import eric.giangiulio.maintenanceinspectionapplication.R
import eric.giangiulio.maintenanceinspectionapplication.Repository.Task
import java.text.DateFormat

class TaskListAdapter(
    val onItemClick: (id:Int)-> Unit,
    val onDeleteClick: (Task) -> Unit,
    val onCheckBoxClick: (Task) -> Unit
) : ListAdapter<Task, TaskListAdapter.TaskViewHolder>(TasksComparator()) {

// Changed the onItemClick parameter to take an integer id instead of a Task object
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TaskViewHolder { // Inflate the item layout and create a ViewHolder instance
        return TaskViewHolder.create(parent)
    }

    // This will bind the clicks and delete button to the task
    override fun onBindViewHolder(holder: TaskViewHolder, position: Int) { // Get the data at the position and bind it with the ViewHolder
        val current = getItem(position)
        holder.itemView.setOnClickListener {
            current.id?.let { it1 -> onItemClick(it1) }
        }
        // Bind the item to the holder
        holder.bind(current, onCheckBoxClick)
        holder.deleteButton.setOnClickListener {
            onDeleteClick(current)
        }
    }

    class TaskViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val taskTitleView: TextView = itemView.findViewById(R.id.textViewTitle) // Create a new text view for the title
        private val taskContentView: TextView = itemView.findViewById(R.id.textViewContent) // Create a new text view for the content
        private val taskDueDateView: TextView = itemView.findViewById(R.id.textViewDueDate) // Create a new text view for the due date
        val taskCheckBox: CheckBox = itemView.findViewById(R.id.checkBox) // Create a new checkbox for the task
        val deleteButton: AppCompatImageButton = itemView.findViewById(R.id.button_delete) // Create a new button for deleting tasks

        fun bind(task: Task?, onCheckBoxClick: (Task) -> Unit) { // Bind the task data to the views
            if (task != null) {
                taskTitleView.text = task.title
                taskContentView.text = task.content
                taskDueDateView.text = DateFormat.getDateTimeInstance(DateFormat.DEFAULT, DateFormat.SHORT).format(task.dueDate)
                taskCheckBox.isChecked = task.isCompleted

                // Creating a strikethrough effect for completed tasks with a condition
                // that checks if the task is completed or not
                // If the task is completed, the text will have a strikethrough effect
                // If the task is not completed, the strikethrough effect will be removed
                taskTitleView.paintFlags = if (task.isCompleted) {
                    taskTitleView.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
                } else {
                    taskTitleView.paintFlags and Paint.STRIKE_THRU_TEXT_FLAG.inv()
                }
                taskCheckBox.setOnClickListener { onCheckBoxClick(task) }
            }
        }


        companion object {
            fun create(parent: ViewGroup): TaskViewHolder { // Inflate the item layout and create a ViewHolder instance
                val view: View = LayoutInflater.from(parent.context)
                    .inflate(R.layout.recyclerview_item, parent, false)
                return TaskViewHolder(view)
            }
        }
    }

    class TasksComparator : DiffUtil.ItemCallback<Task>() { // Class used to compare if two tasks are the same or have the same content
        override fun areItemsTheSame(oldItem: Task, newItem: Task): Boolean {
            return oldItem === newItem
        }

        override fun areContentsTheSame(oldItem: Task, newItem: Task): Boolean { // Check if the title and content are the same
            return oldItem == newItem // This will return true if the title and content are the same
        }
    }
}