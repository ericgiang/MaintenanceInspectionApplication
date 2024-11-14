package eric.giangiulio.maintenanceinspectionapplication.Repository

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "task_table")
data class Task(
    @PrimaryKey(autoGenerate = true) val id: Int?,
    @ColumnInfo(name = "title") var title:String, // Column info for tasks' title
    @ColumnInfo(name = "content") var content:String, // Column info for content of the task
    @ColumnInfo(name = "isCompleted") var isCompleted: Boolean, // Column info for a boolean value to check if the task is completed,
    @ColumnInfo(name = "dueDate") var dueDate: Long, // Column info for the due date of the task
    @ColumnInfo(name = "notificationId") var notificationId: Int // Column info for the notification ID of the task
)
