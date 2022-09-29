package com.udacity.project4

import android.app.Activity
import android.app.Application
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider.getApplicationContext
import androidx.test.espresso.Espresso
import androidx.test.espresso.IdlingRegistry
import androidx.test.espresso.action.ViewActions.*
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.RootMatchers
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import com.udacity.project4.locationreminders.RemindersActivity
import com.udacity.project4.locationreminders.data.ReminderDataSource
import com.udacity.project4.locationreminders.data.local.LocalDB
import com.udacity.project4.locationreminders.data.local.RemindersLocalRepository
import com.udacity.project4.locationreminders.reminderslist.RemindersListViewModel
import com.udacity.project4.locationreminders.savereminder.SaveReminderViewModel
import com.udacity.project4.util.DataBindingIdlingResource
import com.udacity.project4.util.monitorActivity
import com.udacity.project4.utils.EspressoIdlingResource
import kotlinx.coroutines.runBlocking
import org.hamcrest.CoreMatchers
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.module
import org.koin.test.AutoCloseKoinTest
import org.koin.test.get

@RunWith(AndroidJUnit4::class)
@LargeTest
class RemindersActivityTest : AutoCloseKoinTest() {
    private lateinit var repository: ReminderDataSource
    private lateinit var appContext: Application
    private lateinit var activityScenario: ActivityScenario<RemindersActivity>

    @Before
    fun init() {
        stopKoin()//stop the original app koin
        appContext = getApplicationContext()
        val myModule = module {
            viewModel {
                RemindersListViewModel(
                    appContext,
                    get() as ReminderDataSource
                )
            }
            single {
                SaveReminderViewModel(
                    appContext,
                    get() as ReminderDataSource
                )
            }
            single { RemindersLocalRepository(get()) }
            single { LocalDB.createRemindersDao(appContext) }
        }
        //declare a new koin module
        startKoin {
            modules(listOf(myModule))
        }
        //Get our real repository
        repository = get()

        //clear the data to start fresh
        runBlocking {
            repository.deleteAllReminders()
        }
    }

    private val dataBindingIdlingResource = DataBindingIdlingResource()

    @Before
    fun registerIdlingResource() {
        IdlingRegistry.getInstance().register(EspressoIdlingResource.countingIdlingResource)
        IdlingRegistry.getInstance().register(dataBindingIdlingResource)
    }

    @After
    fun unregisterIdlingResource() {
        IdlingRegistry.getInstance().unregister(EspressoIdlingResource.countingIdlingResource)
        IdlingRegistry.getInstance().unregister(dataBindingIdlingResource)
    }

    @Before
    fun setup() {
        activityScenario = ActivityScenario.launch(RemindersActivity::class.java)
        dataBindingIdlingResource.monitorActivity(activityScenario)
    }

    @After
    fun cleanUp() {
        activityScenario.close()
    }

    @Test
    fun addReminder_ShowSnackbarWhenNoTitle(): Unit = runBlocking {
        Espresso.onView(withId(R.id.noDataTextView))
            .check(matches(isDisplayed()))
        Espresso.onView(withId(R.id.addReminderFAB)).perform(click())

        Espresso.onView(withId(R.id.saveReminder)).perform(click())
        Espresso.onView(withId(com.google.android.material.R.id.snackbar_text))
            .check(matches(withText(R.string.err_enter_title)))
    }

    @Test
    fun addReminder_ShowSnackbarWhenNoLocation(): Unit = runBlocking {
        Espresso.onView(withId(R.id.noDataTextView))
            .check(matches(isDisplayed()))
        Espresso.onView(withId(R.id.addReminderFAB)).perform(click())

        Espresso.onView(withId(R.id.reminderTitle))
            .perform(replaceText("new title"))
        Espresso.onView(withId(R.id.saveReminder)).perform(click())
        Espresso.onView(withId(com.google.android.material.R.id.snackbar_text))
            .check(matches(withText(R.string.err_select_location)))
    }


    @Test
    fun addReminder_Successful(): Unit = runBlocking {
        Espresso.onView(withId(R.id.noDataTextView))
            .check(matches(isDisplayed()))
        Espresso.onView(withId(R.id.addReminderFAB)).perform(click())
        Espresso.onView(withId(R.id.reminderTitle))
            .perform(replaceText("new title"))
        Espresso.onView(withId(R.id.reminderDescription))
            .perform(replaceText("new description"))
        Espresso.onView(withId(R.id.selectLocation)).perform(click())
        Espresso.onView(withId(R.id.google_map)).perform(longClick())
        Espresso.onView(withId(R.id.confirmButton)).perform(click())
        Espresso.onView(withId(R.id.saveReminder)).perform(click())
        Espresso.onView(withText(R.string.reminder_saved)).inRoot(
            RootMatchers.withDecorView(
                CoreMatchers.not(
                    CoreMatchers.`is`(
                        getActivity(activityScenario)!!.window.decorView
                    )
                )
            )
        )
            .check(matches(isDisplayed()))

//      Give it sometime to load
        Thread.sleep(2000)

        Espresso.onView(withId(R.id.noDataTextView))
            .check(matches(CoreMatchers.not(isDisplayed())))
    }

    private fun getActivity(activityScenario: ActivityScenario<RemindersActivity>): Activity? {
        var activity: Activity? = null
        activityScenario.onActivity {
            activity = it
        }
        return activity
    }

}
