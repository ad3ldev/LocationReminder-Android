package com.udacity.project4.locationreminders.data.local

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
import com.udacity.project4.locationreminders.data.ReminderDataSource
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.data.dto.Result
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.CoreMatchers.instanceOf
import org.hamcrest.MatcherAssert.assertThat
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
//Medium Test to test the repository
@MediumTest
class RemindersLocalRepositoryTest {
    private lateinit var localDataSource: ReminderDataSource
    private lateinit var database: RemindersDatabase

    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    //  Setting up the repository
    @Before
    fun setup() {
        // using an in-memory database for testing, since it doesn't survive killing the process
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            RemindersDatabase::class.java
        )
            .allowMainThreadQueries()
            .build()

        // creating the datasource repository
        localDataSource =
            RemindersLocalRepository(
                database.reminderDao(),
                Dispatchers.Main
            )
    }

    //  cleanup after every test
    @After
    fun cleanUp() {
        //closing up the database
        database.close()
    }

    // Testing saving and retrieving a reminder by ID
    @Test
    fun saveReminder_retrievesReminder() = runBlocking {
        // GIVEN - a new reminder saved in the database
        // A dummy reminder to test the saving of the database
        val reminder = ReminderDTO("title", "description", "location", 0.0, 0.0, "id")
        // saving the dummy text you the repository
        localDataSource.saveReminder(reminder)

        // WHEN  - Reminder retrieved by ID
        // retrieving the data from the repository
        val result = localDataSource.getReminder(reminder.id)

        // THEN - Same reminder is returned
        // check if result from the repository has a Success status
        assertThat(result, instanceOf(Result.Success::class.java))
        result as Result.Success
        // Checking if the retrieved is equal to the actual data from the repository is the same as the original data
        // Checking ID
        assertThat(result.data.id, `is`(reminder.id))
        // Checking Title
        assertThat(result.data.title, `is`(reminder.title))
        // Checking Description
        assertThat(result.data.description, `is`(reminder.description))
        // Checking location
        assertThat(result.data.location, `is`(reminder.location))
        // Checking Longitude and latitude
        assertThat(result.data.latitude, `is`(reminder.latitude))
        assertThat(result.data.longitude, `is`(reminder.longitude))
    }

    // Testing saving and retrieving a reminder that doesn't exist
    @Test
    fun retrievesNotFoundReminder() = runBlocking {
        // WHEN  - a ReminderID was is not in repository
        // looking for a reminder that doesn't exist
        val result = localDataSource.getReminder("not-found")

        // THEN - Returns Error
        // asserting the error
        assertThat(result, instanceOf(Result.Error::class.java))
    }
}