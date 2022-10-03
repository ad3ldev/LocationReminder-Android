package com.udacity.project4.locationreminders.reminderslist

import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
import com.udacity.project4.R
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito

@RunWith(AndroidJUnit4::class)
@ExperimentalCoroutinesApi
//UI Testing
@MediumTest
class ReminderListFragmentTest {

    // Testing the navigation to add a reminder
    @Test
    fun navigateToAddReminderScreen() {
        // Creating a scenario to use as a fragment
        val scenario = launchFragmentInContainer<ReminderListFragment>(null, R.style.AppTheme)
        // a mock navController
        val navController = Mockito.mock(NavController::class.java)
        // setting the navigation controller of the scenario as the mock navController
        scenario.onFragment {
            Navigation.setViewNavController(it.view!!, navController)
        }
        // on the view with id "addReminderFAB" perform a click
        onView(withId(R.id.addReminderFAB)).perform(click())
        // verify with mockito if the navcontroller navigated from Reminder list fragment to save reminder fragment
        Mockito.verify(navController).navigate(
            ReminderListFragmentDirections.toSaveReminder()
        )
    }

    // Testing if there are no reminders the correct screen is displayed
    @Test
    fun activeReminderList_NoDataDisplayed() {
        // Creating a scenario to use as a fragment
        launchFragmentInContainer<ReminderListFragment>(null, R.style.AppTheme)
        // On view with id noDataTextView which says there is no data on the screen is actually present since there is no data
        onView(withId(R.id.noDataTextView)).check(matches(isDisplayed()))
    }
}

