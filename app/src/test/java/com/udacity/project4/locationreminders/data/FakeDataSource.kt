package com.udacity.project4.locationreminders.data

import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.data.dto.Result

//Use FakeDataSource that acts as a test double to the LocalDataSource
class FakeDataSource(private val reminders: MutableList<ReminderDTO>? = mutableListOf()) :
    ReminderDataSource {

    override suspend fun getReminders(): Result<List<ReminderDTO>> {
        reminders?.let { return Result.Success(ArrayList(it)) }
        return Result.Error(
            Exception("Reminders not found").toString()
        )
    }

    override suspend fun saveReminder(reminder: ReminderDTO) {
        reminders?.add(reminder)
    }

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

    override suspend fun deleteAllReminders() {
        reminders?.clear()
    }
}