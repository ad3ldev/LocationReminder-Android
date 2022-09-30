package com.udacity.project4.locationreminders.data.local

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.core.app.ApplicationProvider.getApplicationContext
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.SmallTest;
import com.udacity.project4.locationreminders.data.dto.ReminderDTO

import org.junit.Before;
import org.junit.Rule;
import org.junit.runner.RunWith;

import kotlinx.coroutines.ExperimentalCoroutinesApi;
import kotlinx.coroutines.test.runBlockingTest
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.CoreMatchers.notNullValue
import org.hamcrest.MatcherAssert.assertThat
import org.junit.After
import org.junit.Test

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
//Unit test the DAO
@SmallTest
class RemindersDaoTest {
    private lateinit var database: RemindersDatabase
    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()


    //    Initializing the database before each test
    @Before
    fun initDb() {
        database = Room.inMemoryDatabaseBuilder(
            getApplicationContext(),
            RemindersDatabase::class.java
        ).build()
    }
    @After
    fun closeDb() = database.close()

    //    Testing that the Dao saves a reminder when given by id
    @Test
    fun insertReminderAndGetById() = runBlockingTest {
        // GIVEN - insert a reminder
        val reminder = ReminderDTO("title", "description", "location", 0.0, 0.0, "id")
        database.reminderDao().saveReminder(reminder)

        // WHEN - Get the reminder by id from the database
        val loaded = database.reminderDao().getReminderById(reminder.id)

        // THEN - The loaded data contains the expected values
        assertThat<ReminderDTO>(loaded as ReminderDTO, notNullValue())
        assertThat(loaded.id, `is`(reminder.id))
        assertThat(loaded.title, `is`(reminder.title))
        assertThat(loaded.description, `is`(reminder.description))
        assertThat(loaded.location, `is`(reminder.location))
        assertThat(loaded.latitude, `is`(reminder.latitude))
        assertThat(loaded.longitude, `is`(reminder.longitude))
    }
    //    Testing that the Dao updates a reminder when given the same id
    @Test
    fun updateReminderAndGetById() = runBlockingTest {
        // When inserting a reminder
        val originalReminder = ReminderDTO("title", "description", "location", 0.0, 0.0, "id")
        database.reminderDao().saveReminder(originalReminder)

        // When the reminder is updated
        val updatedReminder = ReminderDTO("newTitle", "newDescription", "newLocation", 10.0, 10.0, "id")
        database.reminderDao().saveReminder(updatedReminder)

        // THEN - The loaded data contains the expected values
        val loaded = database.reminderDao().getReminderById(originalReminder.id)

        assertThat<ReminderDTO>(loaded as ReminderDTO, notNullValue())
        assertThat(loaded.id, `is`(updatedReminder.id))
        assertThat(loaded.title, `is`(updatedReminder.title))
        assertThat(loaded.description, `is`(updatedReminder.description))
        assertThat(loaded.location, `is`(updatedReminder.location))
        assertThat(loaded.latitude, `is`(updatedReminder.latitude))
        assertThat(loaded.longitude, `is`(updatedReminder.longitude))
    }
}