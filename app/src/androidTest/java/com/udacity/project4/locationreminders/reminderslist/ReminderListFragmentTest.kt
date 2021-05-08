package com.udacity.project4.locationreminders.reminderslist

import android.app.Application
import android.os.Bundle
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
import com.udacity.project4.R
import com.udacity.project4.locationreminders.data.ReminderDataSource
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.data.local.LocalDB
import com.udacity.project4.locationreminders.data.local.RemindersLocalRepository
import com.udacity.project4.locationreminders.savereminder.SaveReminderViewModel
import com.udacity.project4.util.newReminderDTO
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runBlockingTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.module
import org.koin.test.AutoCloseKoinTest
import org.koin.test.get
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify

@RunWith(AndroidJUnit4::class)
@ExperimentalCoroutinesApi
@MediumTest
class ReminderListFragmentTest : AutoCloseKoinTest(){

    private lateinit var reminderDataSource: ReminderDataSource
    private lateinit var appContext: Application

    @Before
    fun init() {
        stopKoin()
        appContext = ApplicationProvider.getApplicationContext()

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
            single {
                RemindersLocalRepository(get()) as ReminderDataSource
            }
            single {
                LocalDB.createRemindersDao(appContext)
            }
        }

        startKoin {
            modules(listOf(myModule))
        }

        reminderDataSource = get()

        //cleanup
        runBlocking {
            reminderDataSource.deleteAllReminders()
        }
    }

    @Test
    fun addReminder_click_navigateToSaveReminderFragment() {
        val scenario = launchFragmentInContainer<ReminderListFragment>(Bundle(), R.style.AppTheme)
        val navController = mock(NavController::class.java)

        scenario.onFragment {
            Navigation.setViewNavController(it.view!!, navController)
        }

        onView(withId(R.id.addReminderFAB)).perform(click())

        verify(navController).navigate(
            ReminderListFragmentDirections.toSaveReminder()
        )
    }

    @Test
    fun addTwoReminders_bothDisplayed() {
        val reminder1 = newReminderDTO()
        val reminder2 = newReminderDTO()

        runBlocking {
            reminderDataSource.saveReminder(reminder1)
            reminderDataSource.saveReminder(reminder2)
        }

        launchFragmentInContainer<ReminderListFragment>(Bundle(), R.style.AppTheme)

        onView(ViewMatchers.withText(reminder1.title)).check(
            ViewAssertions.matches(ViewMatchers.isDisplayed()))

        onView(ViewMatchers.withText(reminder1.description)).check(
            ViewAssertions.matches(ViewMatchers.isDisplayed()))

        onView(ViewMatchers.withText(reminder1.location)).check(
            ViewAssertions.matches(ViewMatchers.isDisplayed()))

        onView(ViewMatchers.withText(reminder2.title)).check(
            ViewAssertions.matches(ViewMatchers.isDisplayed()))

        onView(ViewMatchers.withText(reminder2.description)).check(
            ViewAssertions.matches(ViewMatchers.isDisplayed()))

        onView(ViewMatchers.withText(reminder2.location)).check(
            ViewAssertions.matches(ViewMatchers.isDisplayed()))
    }

    @Test
    fun reminderListFragment_noData_errorSnackBackShown() = runBlockingTest {
        launchFragmentInContainer<ReminderListFragment>(Bundle(), R.style.AppTheme)

        onView(ViewMatchers.withText(R.string.no_data))
                .check(ViewAssertions.matches(ViewMatchers.isDisplayed()))
    }
}