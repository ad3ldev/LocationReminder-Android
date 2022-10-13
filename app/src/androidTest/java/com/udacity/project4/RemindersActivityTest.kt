package com.udacity.project4

import ToastMatcher
import android.app.Application
import android.view.View
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider.getApplicationContext
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.IdlingRegistry
import androidx.test.espresso.action.ViewActions.*
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.rules.ActivityScenarioRule
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
import org.hamcrest.CoreMatchers.not
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
class RemindersActivityTest :
    AutoCloseKoinTest() {
    // Extended Koin Test - embed autoclose @after method to close Koin after every test
    private lateinit var repository: ReminderDataSource
    private lateinit var appContext: Application

    /**
     * As we use Koin as a Service Locator Library to develop our code, we'll also use Koin to test our code.
     * at this step we will initialize Koin related code to be able to use it in out testing.
     */
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
            single { RemindersLocalRepository(get()) as ReminderDataSource }
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
        // register counting the idle resources
        IdlingRegistry.getInstance().register(EspressoIdlingResource.countingIdlingResource)
        IdlingRegistry.getInstance().register(dataBindingIdlingResource)
    }

    @After
    fun unregisterIdlingResource() {
        // unregister counting the idle resources
        IdlingRegistry.getInstance().unregister(EspressoIdlingResource.countingIdlingResource)
        IdlingRegistry.getInstance().unregister(dataBindingIdlingResource)
    }

    // Testing if the snackbar appears when there is no title added
    @Test
    fun addReminder_ShowSnackbarWhenNoTitle() = runBlocking {
        // launching an activity scenario from Reminder Activity
        val activityScenario = ActivityScenario.launch(RemindersActivity::class.java)
        // bind the idling recources to monitor the activity that was launched
        dataBindingIdlingResource.monitorActivity(activityScenario)

        // Check if there is no data on the screen.
        onView(withId(R.id.noDataTextView)).check(matches(isDisplayed()))
        // Click on the add Reminder FAB
        onView(withId(R.id.addReminderFAB)).perform(click())

        // Perform a click on save reminder with no data input
        onView(withId(R.id.saveReminder)).perform(click())
        // Check if the snackbar appears and says to input data
        onView(withId(com.google.android.material.R.id.snackbar_text)).check(matches(withText(R.string.err_enter_title)))

        // Closing the activity
        activityScenario.close()
        // Wait for any toasts to disappear
        Thread.sleep(2500)
    }

    // Testing if the snackbar appears when there is no location added
    @Test
    fun addReminder_ShowSnackbarWhenNoLocation() = runBlocking {
        // launching an activity scenario from Reminder Activity
        val activityScenario = ActivityScenario.launch(RemindersActivity::class.java)
        // bind the idling recources to monitor the activity that was launched
        dataBindingIdlingResource.monitorActivity(activityScenario)

        // Check if there is no data on the screen.
        onView(withId(R.id.noDataTextView)).check(matches(isDisplayed()))
        // Click on the add Reminder FAB
        onView(withId(R.id.addReminderFAB)).perform(click())
        // Input a title
        onView(withId(R.id.reminderTitle)).perform(replaceText("new title"))
        // Click on save
        onView(withId(R.id.saveReminder)).perform(click())
        // Snackbar of that there is no location chosen
        onView(withId(com.google.android.material.R.id.snackbar_text)).check(matches(withText(R.string.err_select_location)))

        // Closing the activity
        activityScenario.close()
        // Wait for any toasts to disappear
        Thread.sleep(2500)
    }

    // Testing if a reminder can be added successfully
    @Test
    fun addReminder_Successful() = runBlocking {
        // launching an activity scenario from Reminder Activity
        val activityScenario = ActivityScenario.launch(RemindersActivity::class.java)
        // bind the idling recources to monitor the activity that was launched
        dataBindingIdlingResource.monitorActivity(activityScenario)
        var decorView: View? = null
        activityScenario.onActivity {
            decorView = it.window.decorView
        }

        // Check if there is no data on the screen.
        onView(withId(R.id.noDataTextView)).check(matches(isDisplayed()))
        // Click on the add Reminder FAB
        onView(withId(R.id.addReminderFAB)).perform(click())

        // Input a title
        onView(withId(R.id.reminderTitle)).perform(replaceText("new title"))
        // Input a description
        onView(withId(R.id.reminderDescription)).perform(replaceText("new description"))
        // Start selecting the location
        onView(withId(R.id.selectLocation)).perform(click())
        // Choose a location with a long click
        onView(withId(R.id.google_map)).perform(longClick())
        // Confirm the location of reminder
        onView(withId(R.id.confirmButton)).perform(click())
        // Save the reminder
        onView(withId(R.id.saveReminder)).check(matches(isDisplayed())).perform(click())
        // Check Toast
        onView(withText(R.string.reminder_saved)).inRoot(ToastMatcher())
            .check(matches(isDisplayed()));
        // Check if there is any data displayed and it's not empty
        onView(withId(R.id.noDataTextView)).check(matches(not(isDisplayed())))

        // close activity
        activityScenario.close()
        // Wait for any toasts to disappear
        Thread.sleep(2500)
    }
}
