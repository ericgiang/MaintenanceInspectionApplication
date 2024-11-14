package eric.giangiulio.maintenanceinspectionapplication

import android.app.Application
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import eric.giangiulio.maintenanceinspectionapplication.Repository.TaskRepository
import eric.giangiulio.maintenanceinspectionapplication.Repository.TaskRoomDatabase
class TasksApplication : Application() {

    val applicationScope = CoroutineScope(SupervisorJob())
    // Using by lazy so the database and the repository are only created when they're needed
    // rather than when the application starts
    val database by lazy { TaskRoomDatabase.getDatabase(this,applicationScope) }
    val repository by lazy { TaskRepository(database.taskDao()) }
}