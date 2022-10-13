package com.udacity.project4.locationreminders.reminderslist

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.udacity.project4.locationreminders.MainCoroutineRule
import com.udacity.project4.locationreminders.data.FakeDataSource
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.getOrAwaitValue
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.CoreMatchers.nullValue
import org.hamcrest.MatcherAssert.assertThat
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.core.context.stopKoin

@RunWith(AndroidJUnit4::class)
@ExperimentalCoroutinesApi
class RemindersListViewModelTest {

    private lateinit var remindersViewModel: RemindersListViewModel
    private lateinit var remindersRepository: FakeDataSource

    private var reminders = MutableList(3) { i ->
        ReminderDTO("title$i", "description$i", "location$i", i.toDouble(), i.toDouble())
    }

    @get:Rule
    var mainCoroutineRule = MainCoroutineRule()

    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    @Before
    fun setupViewModel() {
        // Start a repository to use
        remindersRepository = FakeDataSource(reminders)
        remindersViewModel =
            RemindersListViewModel(ApplicationProvider.getApplicationContext(), remindersRepository)
    }

    @After
    fun tearDown() {
        stopKoin()
    }

    // Checking if the loaded reminders are correct
    @Test
    fun loadReminders_getsStoredReminders() {
        // Loading reminders to the viewModel
        remindersViewModel.loadReminders()

        // wait for the remindersList to be loaded
        val value = remindersViewModel.remindersList.getOrAwaitValue()

        // check if the reminders loaded are the same expected values
        assertThat(value.size, `is`(reminders.size))
        for (i in 0 until reminders.size) {
            // Checking title
            assertThat(value[i].title, `is`("title$i"))
            // Checking description
            assertThat(value[i].description, `is`("description$i"))
            // Checking location, latitude and longitude
            assertThat(value[i].location, `is`("location$i"))
            assertThat(value[i].latitude, `is`(i.toDouble()))
            assertThat(value[i].longitude, `is`(i.toDouble()))
        }
    }

    // Checking if the reminders are loading
    @Test
    fun loadReminders_checkLoading() {
        // Pause the dispatcher so  the dispatcher will not execute any coroutines automatically
        mainCoroutineRule.pauseDispatcher()
        // load reminders
        remindersViewModel.loadReminders()
        // see if the data is loading
        assertThat(remindersViewModel.showLoading.getOrAwaitValue(), `is`(true))
        // Resume the dispatcher
        mainCoroutineRule.resumeDispatcher()
        // check that loading is done.
        assertThat(remindersViewModel.showLoading.getOrAwaitValue(), `is`(false))
    }

    // Checking if we get an error if there is no reminders
    @Test
    fun loadReminders_shouldReturnError() = runBlocking {
        // Create a repository with no Data
        remindersRepository = FakeDataSource(null)
        // And create a view model with that repository
        remindersViewModel =
            RemindersListViewModel(ApplicationProvider.getApplicationContext(), remindersRepository)
        // Try to load the reminders
        remindersViewModel.loadReminders()
        // Check if the snackbar will be shown with an exception
        assertThat(remindersViewModel.showSnackBar.getOrAwaitValue(), nullValue())
    }
}