package eric.giangiulio.maintenanceinspectionapplication.Util

import android.Manifest
import android.app.Activity
import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import androidx.core.app.TaskStackBuilder
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import eric.giangiulio.maintenanceinspectionapplication.AlarmReceiver
import eric.giangiulio.maintenanceinspectionapplication.R
import eric.giangiulio.maintenanceinspectionapplication.Repository.Task

class NotificationUtil {
    val CHANNEL_ID = "TaskList Notification Channel"

    fun createNotificationChannel(context: Context) {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        val name = context.getString(R.string.channel_name)
        val descriptionText = context.getString(R.string.channel_description)
        val importance = NotificationManager.IMPORTANCE_DEFAULT
        val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
            description = descriptionText
        }
        // Register the channel with the system
        val notificationManager: NotificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(channel)
    }


    // Function to cancel the notification based on the specific task
    fun cancelNotification(context: Context, task: Task) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager
        val alarmIntent = Intent(context.applicationContext, AlarmReceiver::class.java).apply {
            putExtra("TASK_ID", task.id)
            putExtra("TASK_TITLE", task.title)
            putExtra("TASK_CONTENT", task.content)
        }
        val pendingIntent = PendingIntent.getBroadcast(context.applicationContext, task.notificationId, alarmIntent, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)
        Log.d("NotificationUtil", "Canceling notification with ID: ${task.notificationId}")
        alarmManager?.cancel(pendingIntent)
    }

    fun createClickableNotification(context: Context, title: String, content: String, clickIntent: Intent, id: Int) {
        val stackBuilder: TaskStackBuilder = TaskStackBuilder.create(context)
        stackBuilder.addNextIntentWithParentStack(clickIntent)
        val pendingClickIntent = stackBuilder.getPendingIntent(id,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)

        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher_round)
            .setContentTitle(title)
            .setContentText(content)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingClickIntent)
            .setAutoCancel(true)

        with(NotificationManagerCompat.from(context)) {
            // notificationId is a unique int for each notification that you must define
            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(
                    (context as Activity),
                    arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                    1
                )
                return
            }
            notify(id, builder.build())
        }
    }
}