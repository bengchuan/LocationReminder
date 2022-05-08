package com.udacity.project4.locationreminders.data

import android.util.Log
import androidx.annotation.VisibleForTesting
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.data.dto.Result


enum class ErrorMessage(val message: String) {
    ERROR_MSG_GET_REMINDERS("getReminders() error"),
    ERROR_MSG_GET_REMINDER("getReminder() error"),
    ERROR_MSG_SAVE_REMINDER("saveReminder() error")
}

// Use FakeDataSource that acts as a test double to the LocalDataSource
// Use VisibleForTesting to ensure prod code will not have access to this fakedatasource.
@VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
class FakeDataSource(var shouldReturnError: Boolean = false) : ReminderDataSource {

    companion object {
        private const val TAG = "FakeDataSource"
    }

    private var reminders = mutableListOf<ReminderDTO>()

    override suspend fun getReminders(): Result<List<ReminderDTO>> {
        if (shouldReturnError) return Result.Error(ErrorMessage.ERROR_MSG_GET_REMINDERS.message)
        Log.d(TAG, "Returning list of reminders of size: ${reminders.size}")
        return Result.Success(reminders)
    }

    override suspend fun saveReminder(reminder: ReminderDTO) {
        Log.d(TAG, "Reminder has been added.")
        reminders.add(reminder)
    }

    override suspend fun getReminder(id: String): Result<ReminderDTO> {
        if (shouldReturnError) return Result.Error(ErrorMessage.ERROR_MSG_GET_REMINDER.message)
        val result = reminders.find { it.id == id }
        return if (result != null) {
            Log.d(TAG, "Found reminder with $id ")
            Result.Success(result)
        } else {
            Log.d(TAG, "Cannot find reminder with $id ")
            Result.Error("Failed to find reminder with id: $id")
        }
    }

    override suspend fun deleteAllReminders() {
        Log.d(TAG, "FakeDataSource has been cleared.")
        reminders.clear()
    }
}