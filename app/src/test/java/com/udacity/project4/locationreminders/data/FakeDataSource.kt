package com.udacity.project4.locationreminders.data

import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.data.dto.Result

//Use FakeDataSource that acts as a test double to the LocalDataSource

// A class to replace the actual data source to act as a test double
class FakeDataSource(private val reminders: MutableList<ReminderDTO> = mutableListOf()) :
    ReminderDataSource {

    private var shouldReturnError = false

    fun setShouldReturnError(shouldReturn: Boolean) {
        this.shouldReturnError = shouldReturn
    }

    // Over riding the get reminders method to make it return success if there is any reminders
    // and returns an empty list if there is no reminders
    override suspend fun getReminders(): Result<List<ReminderDTO>> {
        if(shouldReturnError) {
            return Result.Error("Unable to retrieve reminders")
        }
        return Result.Success(ArrayList(reminders))
    }

    // Overriding the method in ReminderDataSource to save the reminder in the mutable list Reminders
    override suspend fun saveReminder(reminder: ReminderDTO) {
        reminders.add(reminder)
    }

    // Overriding to get the reminder using the filter method
    override suspend fun getReminder(id: String): Result<ReminderDTO> {
        if (shouldReturnError) {
            return Result.Error("Error")
        }
        val reminder = reminders.find { it.id == id }
        return if (reminder != null) {
            Result.Success(reminder)
        } else {
            Result.Error("Reminder not found!")
        }
    }

    // Clearing the reminders mutable list
    override suspend fun deleteAllReminders() {
        reminders.clear()
    }
}