package com.udacity.project4.locationreminders.reminderslist

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.udacity.project4.locationreminders.MainCoroutineRule
import com.udacity.project4.locationreminders.data.FakeDataSource
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.getOrAwaitValue
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.hamcrest.CoreMatchers.`is`
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
        remindersViewModel.loadReminders()

        val value = remindersViewModel.remindersList.getOrAwaitValue()
        assertThat(value.size, `is`(reminders.size))
        for (i in 0 until reminders.size) {
            assertThat(value[i].title, `is`("title$i"))
            assertThat(value[i].description, `is`("description$i"))
            assertThat(value[i].location, `is`("location$i"))
            assertThat(value[i].latitude, `is`(i.toDouble()))
            assertThat(value[i].longitude, `is`(i.toDouble()))
        }
    }

    // Checking if the reminders are loading
    @Test
    fun loadReminders_checkLoading() {
        mainCoroutineRule.pauseDispatcher()
        remindersViewModel.loadReminders()
        assertThat(remindersViewModel.showLoading.getOrAwaitValue(), `is`(true))
        mainCoroutineRule.resumeDispatcher()
        assertThat(remindersViewModel.showLoading.getOrAwaitValue(), `is`(false))
    }

    // Checking if we get an error if there is no reminders
    @Test
    fun loadReminders_getRemindersError() {
        reminders.clear()
        remindersRepository = FakeDataSource(null)
        remindersViewModel =
            RemindersListViewModel(ApplicationProvider.getApplicationContext(), remindersRepository)
        remindersViewModel.loadReminders()
        assertThat(remindersViewModel.showSnackBar.getOrAwaitValue(), `is`("java.lang.Exception: Reminders not found"))
    }
}