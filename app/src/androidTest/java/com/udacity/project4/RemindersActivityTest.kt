package com.udacity.project4

import android.app.Application
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider.getApplicationContext
import androidx.test.espresso.Espresso
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.assertion.ViewAssertions
import androidx.test.espresso.matcher.RootMatchers
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import com.udacity.project4.locationreminders.RemindersActivity
import com.udacity.project4.locationreminders.data.ReminderDataSource
import com.udacity.project4.locationreminders.data.local.LocalDB
import com.udacity.project4.locationreminders.data.local.RemindersLocalRepository
import com.udacity.project4.locationreminders.reminderslist.RemindersListViewModel
import com.udacity.project4.locationreminders.savereminder.SaveReminderViewModel
import com.udacity.project4.util.monitorActivity
import kotlinx.coroutines.runBlocking
import org.hamcrest.CoreMatchers
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
//END TO END test to black box test the app
class RemindersActivityTest :
    AutoCloseKoinTest() {// Extended Koin Test - embed autoclose @after method to close Koin after every test

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

    @Test
    fun createReminder_noTitle_errorToEnterTitle() {
        val activityScenario = ActivityScenario.launch(RemindersActivity::class.java)
        activityScenario.use {
            Espresso.onView(withId(R.id.noDataTextView))
                .check(ViewAssertions.matches(ViewMatchers.isDisplayed()))

            Espresso.onView(withId(R.id.addReminderFAB)).perform(ViewActions.click())
            Espresso.onView(withId(R.id.saveReminder)).perform(ViewActions.click())

            Espresso.onView(withId(com.google.android.material.R.id.snackbar_text))
                .check(ViewAssertions.matches(withText(R.string.err_enter_title)))
        }
    }

    @Test
    fun createReminder_WithoutLocation() {
        val activityScenario = ActivityScenario.launch(RemindersActivity::class.java)
        activityScenario.use {
            Espresso.onView(withId(R.id.noDataTextView))
                .check(ViewAssertions.matches(ViewMatchers.isDisplayed()))

            Espresso.onView(withId(R.id.addReminderFAB)).perform(ViewActions.click())

            Espresso.onView(withId(R.id.reminderTitle))
                .perform(ViewActions.replaceText("Title"))

            Espresso.onView(withId(R.id.saveReminder)).perform(ViewActions.click())

            Espresso.onView(withId(com.google.android.material.R.id.snackbar_text))
                .check(ViewAssertions.matches(withText(R.string.err_select_location)))
        }
    }

    @Test
    fun createReminder() {
        val activityScenario = ActivityScenario.launch(RemindersActivity::class.java)
        activityScenario.use {
            Espresso.onView(withId(R.id.noDataTextView))
                .check(
                    ViewAssertions.matches(
                        ViewMatchers.withEffectiveVisibility(
                            ViewMatchers.Visibility.VISIBLE
                        )
                    )
                )

            Espresso.onView(withId(R.id.addReminderFAB)).perform(ViewActions.click())
            Espresso.onView(withId(R.id.selectLocation)).perform(ViewActions.click())

            Espresso.onView(withId(R.id.save_button)).perform(ViewActions.click())
            Espresso.onView(withId(R.id.reminderTitle)).perform(
                ViewActions.typeText("Title")
            )

            Espresso.onView(withId(R.id.reminderDescription))
                .perform(ViewActions.typeText("Description"))

            Espresso.closeSoftKeyboard()

            Espresso.onView(withId(R.id.saveReminder)).perform(ViewActions.click())

            Espresso.onView(withText("Title"))
                .check(ViewAssertions.matches(ViewMatchers.isDisplayed()))

            Espresso.onView(withText("Description"))
                .check(ViewAssertions.matches(ViewMatchers.isDisplayed()))
        }
    }
}
