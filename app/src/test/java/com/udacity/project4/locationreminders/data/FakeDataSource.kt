package com.udacity.project4.locationreminders.data

import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.data.dto.Result

//Use FakeDataSource that acts as a test double to the LocalDataSource

// A class to replace the actual data source to act as a test double
class FakeDataSource(private val reminders: MutableList<ReminderDTO>? = mutableListOf()) :
    ReminderDataSource {
    // Over riding the get reminders method to make it return success if there is any reminders
    // and returns an empty list if there is no reminders
    override suspend fun getReminders(): Result<List<ReminderDTO>> {
        return try {
            Result.Success(ArrayList(reminders!!))
        } catch (ex: Exception) {
            Result.Error(ex.localizedMessage)
        }
    }

    // Overriding the method in ReminderDataSource to save the reminder in the mutable list Reminders
    override suspend fun saveReminder(reminder: ReminderDTO) {
        reminders?.add(reminder)
    }

    // Overriding to get the reminder using the filter method
    override suspend fun getReminder(id: String): Result<ReminderDTO> {
        return try {
            val reminder = reminders?.find{x-> x.id == id}
            if (reminder != null) {
                Result.Success(reminder)
            } else {
                Result.Error("Reminder not found!")
            }
        } catch (e: Exception) {
            Result.Error(e.localizedMessage)
        }
    }

    // Clearing the reminders mutable list
    override suspend fun deleteAllReminders() {
        reminders?.clear()
    }
}