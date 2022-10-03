package com.udacity.project4.locationreminders.savereminder

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.viewModelScope
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.udacity.project4.locationreminders.MainCoroutineRule
import com.udacity.project4.locationreminders.data.FakeDataSource
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.data.dto.Result
import com.udacity.project4.locationreminders.getOrAwaitValue
import com.udacity.project4.locationreminders.reminderslist.ReminderDataItem
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import org.hamcrest.CoreMatchers
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.CoreMatchers.nullValue
import org.hamcrest.MatcherAssert
import org.hamcrest.MatcherAssert.assertThat
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.core.Koin
import org.koin.core.context.stopKoin

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
class SaveReminderViewModelTest {
    private lateinit var reminders: MutableList<ReminderDTO>
    private lateinit var dataSource: FakeDataSource
    private lateinit var viewModel: SaveReminderViewModel

    @get:Rule
    var mainCoroutineRule = MainCoroutineRule()

    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    @Before
    fun setup() {
        reminders = mutableListOf()
        dataSource = FakeDataSource(reminders)
        viewModel =
            SaveReminderViewModel(ApplicationProvider.getApplicationContext(), dataSource)
    }

    @After
    fun cleanUp() {
        reminders.clear()
        viewModel.onClear()
        stopKoin()
    }

    // Testing if the validation of data works correctly
    @Test
    fun validateEnteredData() {
        // invalid reminder created
        val invalidReminder = ReminderDataItem("", "", "", 0.0, 0.0)
        // valid reminder created
        val validReminder = ReminderDataItem("title", "description", "location", 0.0, 0.0)

        // Trying to insert the invalid data should return a false
        assertThat(viewModel.validateEnteredData(invalidReminder), `is`(false))
        // Trying to insert the valid data should return a true
        assertThat(viewModel.validateEnteredData(validReminder), `is`(true))
    }

    // Testing if it saves the reminder correctly
    @Test
    fun saveReminder() {
        // Dummy reminder to use for testing
        val reminder = ReminderDataItem("title", "description", "location", 0.0, 0.0, "id")

        // check if the reminder entered is valid
        require(viewModel.validateEnteredData(reminder))
        // save it to the view model
        viewModel.saveReminder(reminder)

        // create a reminder result that has the default value of an Error
        var reminderById: Result<ReminderDTO> = Result.Error("not initialized")

        // coroutine to get the the dummy reminder
        viewModel.viewModelScope.launch {
            reminderById = dataSource.getReminder("id")
        }

        // Assert that the data got from the dataSource is correct
        assert(reminderById is Result.Success)
        (reminderById as Result.Success).data.apply {
            assertThat(title,`is`("title"))
            assertThat(description, `is`("description"))
            assertThat(location,`is`("location"))
            assertThat(latitude,`is`(0.0))
            assertThat(longitude, `is`(0.0))
            assertThat(id,`is`("id"))
        }
    }

    // Checking if onClear of the viewModel works
    @Test
    fun clearSelectedReminder() {
        // put some dummy info in the variables inside the viewModel
        viewModel.apply {
            reminderTitle.value = "title"
            reminderDescription.value = "description"
            reminderSelectedLocationStr.value = "location"
            latitude.value = 0.0
            longitude.value = 0.0
        }

        // Clear viewModel
        viewModel.onClear()


        // Check if the view Model is cleared and all is equal null
        viewModel.apply {
            assertThat(reminderTitle.getOrAwaitValue(), nullValue())
            assertThat(reminderDescription.getOrAwaitValue(),nullValue())
            assertThat(reminderSelectedLocationStr.getOrAwaitValue(), nullValue())
            assertThat(latitude.getOrAwaitValue(), nullValue())
            assertThat(longitude.getOrAwaitValue(), nullValue())
        }
    }


}