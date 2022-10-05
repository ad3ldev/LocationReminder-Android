package com.udacity.project4.locationreminders.data

import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.data.dto.Result

//Use FakeDataSource that acts as a test double to the LocalDataSource

// A class to replace the actual data source to act as a test double
class FakeDataSource(private val reminders: MutableList<ReminderDTO>? = mutableListOf()) :
    ReminderDataSource {
    // Over riding the get reminders method to make it return success if there is any reminders
    // and returns error if null
    override suspend fun getReminders(): Result<List<ReminderDTO>> {
        reminders?.let { return Result.Success(ArrayList(it)) }
        return Result.Error(
            Exception("Reminders not found").toString()
        )
    }

    // Overriding the method in ReminderDataSource to save the reminder in the mutable list Reminders
    override suspend fun saveReminder(reminder: ReminderDTO) {
        reminders?.add(reminder)
    }

    // Overriding to get the reminder using the filter method
    override suspend fun getReminder(id: String): Result<ReminderDTO> {
        reminders?.let {
            val res = reminders.filter { it.id == id }
            if (res.isEmpty()) {
                return Result.Error("No result found")
            } else if (res.size > 1) {
                return Result.Error("More than one reminder")
            } else {
                return Result.Success(res[0])
            }
        }
        return Result.Error(
            Exception("Reminders not found").toString()
        )
    }

    // Clearing the reminders mutable list
    override suspend fun deleteAllReminders() {
        reminders?.clear()
    }
}