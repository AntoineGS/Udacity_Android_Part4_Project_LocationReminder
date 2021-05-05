package com.udacity.project4.locationreminders.data

import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.data.dto.Result

//Use FakeDataSource that acts as a test double to the LocalDataSource
class FakeDataSource(var reminders: MutableList<ReminderDTO>? = mutableListOf()) : ReminderDataSource {

    override suspend fun getReminders(): Result<List<ReminderDTO>> {
        reminders?.let {
            if (it.isNotEmpty()) {
                return Result.Success(it)
            }
        }
        return Result.Error("No reminders found")
    }

    override suspend fun saveReminder(reminder: ReminderDTO){
        this.reminders?.add(reminder)
    }

    override suspend fun getReminder(id: String): Result<ReminderDTO> {
        reminders?.firstOrNull {
            it.id == id
        }?.let {
            return Result.Success(it)
        }
        return Result.Error("Reminder not found")
    }

    override suspend fun deleteAllReminders() {
        reminders = mutableListOf()
    }
}