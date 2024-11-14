package eric.giangiulio.maintenanceinspectionapplication.Util
// This object generates unique notification IDs
object NotificationIdGenerator {
    private var currentId = 0
    fun getNextId(): Int {
        return currentId++
    }
}