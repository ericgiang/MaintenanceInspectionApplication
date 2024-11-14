package eric.giangiulio.maintenanceinspectionapplication

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import eric.giangiulio.maintenanceinspectionapplication.TaskDetailActivity.TaskDetailActivity
import eric.giangiulio.maintenanceinspectionapplication.Util.NotificationUtil
class AlarmReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        // This method is called when the BroadcastReceiver is receiving an Intent broadcast.
        Log.d("AlarmReceiver", "Alarm received!")
        val id = intent.getIntExtra("TASK_ID",0)
        val title = intent.getStringExtra("TASK_TITLE") ?: "Task Due!"
        val content = intent.getStringExtra("TASK_CONTENT") ?: "Your is due soon!"
        Log.d("AlarmReceiver", id.toString())
        Log.d("AlarmReceiver", "Notification ID: $id, Title: $title, Content: $content")
        val clickIntent:Intent = Intent(context, TaskDetailActivity::class.java)
        clickIntent.putExtra("TASK_ID",id)
        NotificationUtil().createClickableNotification(context, title, content, clickIntent, id)
    }
}