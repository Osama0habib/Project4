package com.udacity.project4.locationreminders.data

import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.data.dto.Result
import com.udacity.project4.locationreminders.reminderslist.ReminderDataItem

//Use FakeDataSource that acts as a test double to the LocalDataSource
class FakeDataSource(var reminders: MutableList<ReminderDTO> = mutableListOf()) : ReminderDataSource {

//    TODO: Create a fake data source to act as a double to the real data source
    private var shouldReturnError = false

    fun setReturnError(value: Boolean) {
        shouldReturnError = value
    }


    override suspend fun getReminders(): Result<List<ReminderDTO>> {
        if (shouldReturnError) {
            return Result.Error("Test exception")
        }
        return try {
            Result.Success(ArrayList(reminders))
        } catch (ex: Exception) {
            Result.Error(ex.localizedMessage)
        }
    }

    override suspend fun saveReminder(reminder: ReminderDTO) {
        reminders.add(reminder)
    }

    override suspend fun getReminder(id: String): Result<ReminderDTO> {
        if (shouldReturnError) {
            return Result.Error("Test exception")
        }
       val result = reminders.find {
            it.id == id
        }
         result?.let {
            return Result.Success(it)
        }
        return Result.Error("no reminder found",404)
    }

    override suspend fun deleteAllReminders() {

        reminders.clear()
    }


}