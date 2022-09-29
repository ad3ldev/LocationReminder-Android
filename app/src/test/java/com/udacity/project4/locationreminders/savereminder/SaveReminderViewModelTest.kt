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

    @Test
    fun validateEnteredData() {
        val invalidReminder = ReminderDataItem("", "", "", 0.0, 0.0)
        val validReminder = ReminderDataItem("title", "description", "location", 0.0, 0.0)

        assertThat(viewModel.validateEnteredData(invalidReminder), `is`(false))
        assertThat(viewModel.validateEnteredData(validReminder), `is`(true))
    }

    @Test
    fun saveReminder() {
        val reminder = ReminderDataItem("title", "description", "location", 0.0, 0.0, "id")

        require(viewModel.validateEnteredData(reminder))
        viewModel.saveReminder(reminder)

        var reminderById: Result<ReminderDTO> = Result.Error("not initialized")
        viewModel.viewModelScope.launch {
            reminderById = dataSource.getReminder("id")
        }

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

    @Test
    fun clearSelectedReminder() {
        viewModel.apply {
            reminderTitle.value = "title"
            reminderDescription.value = "description"
            reminderSelectedLocationStr.value = "location"
            latitude.value = 0.0
            longitude.value = 0.0
        }

        viewModel.onClear()

        viewModel.apply {
            assertThat(reminderTitle.getOrAwaitValue(), nullValue())
            assertThat(reminderDescription.getOrAwaitValue(),nullValue())
            assertThat(reminderSelectedLocationStr.getOrAwaitValue(), nullValue())
            assertThat(latitude.getOrAwaitValue(), nullValue())
            assertThat(longitude.getOrAwaitValue(), nullValue())
        }
    }


}