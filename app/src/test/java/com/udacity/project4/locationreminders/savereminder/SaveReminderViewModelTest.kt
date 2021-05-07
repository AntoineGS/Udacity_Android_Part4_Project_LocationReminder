package com.udacity.project4.locationreminders.savereminder

import android.app.Application
import android.os.Build
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.udacity.project4.R
import com.udacity.project4.base.NavigationCommand
import com.udacity.project4.locationreminders.data.FakeDataSource
import com.udacity.project4.locationreminders.getOrAwaitValue
import com.udacity.project4.locationreminders.reminderslist.ReminderDataItem
import kotlinx.coroutines.Dispatchers

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestCoroutineDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.core.context.stopKoin
import org.robolectric.annotation.Config

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
@Config(sdk = [Build.VERSION_CODES.Q]) // getting an error running the tests otherwise ¯\_(ツ)_/¯
class SaveReminderViewModelTest {

    private lateinit var context: Application
    private lateinit var reminder: ReminderDataItem
    private lateinit var fakeDataSource: FakeDataSource
    private lateinit var saveReminderViewModel: SaveReminderViewModel

    @ExperimentalCoroutinesApi
    private val testDispatcher = TestCoroutineDispatcher()

    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    @Before
    fun setup() {
        fakeDataSource = FakeDataSource()
        context = ApplicationProvider.getApplicationContext()
        saveReminderViewModel = SaveReminderViewModel(context, fakeDataSource)
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        stopKoin()
        Dispatchers.resetMain()
        testDispatcher.cleanupTestCoroutines()
    }

    private fun newReminder(): ReminderDataItem {
        return ReminderDataItem(
            "Trash",
            "Take out the trash",
            "Home",
            10.00,
            5.00)
    }

    @Test
    fun saveReminder_newReminder_showToastSaved() {
        reminder = newReminder()
        saveReminderViewModel.saveReminder(reminder)
        val showToastValue = saveReminderViewModel.showToast.getOrAwaitValue()
        Assert.assertEquals(showToastValue, context.resources.getString(R.string.reminder_saved))
    }

    @Test
    fun saveReminder_newReminder_navigationBackCommand() {
        reminder = newReminder()
        saveReminderViewModel.saveReminder(reminder)
        val navigationCommand = saveReminderViewModel.navigationCommand.getOrAwaitValue()
        Assert.assertEquals(navigationCommand, NavigationCommand.Back)
    }

    @Test
    fun saveReminder_newReminder_loading() {
        reminder = newReminder()
        testDispatcher.pauseDispatcher()
        saveReminderViewModel.saveReminder(reminder)
        val showLoadingValue = saveReminderViewModel.showLoading.getOrAwaitValue()
        assertThat(showLoadingValue, Matchers.`is`(true))
        testDispatcher.resumeDispatcher()
        val showLoadingValueAgain = saveReminderViewModel.showLoading.getOrAwaitValue()
        assertThat(showLoadingValueAgain, Matchers.`is`(false))
    }

    @Test
    fun validateEnteredData_newReminder_returnsTrue() {
        reminder = newReminder()
        val value = saveReminderViewModel.validateEnteredData(reminder)
        assertThat(value, Matchers.`is`(true))
    }

    @Test
    fun validateEnteredData_emptyTitle_snackBarMatches() {
        reminder = newReminder()
        reminder.title = null
        val value = saveReminderViewModel.validateEnteredData(reminder)
        val snackBarValue = saveReminderViewModel.showSnackBarInt.getOrAwaitValue()
        assertThat(value, Matchers.`is`(false))
        Assert.assertEquals(
            context.getString(snackBarValue),
            context.resources.getString(R.string.err_enter_title)
        )
    }

    @Test
    fun validateEnteredData_emptyLocation_snackBarMatches() {
        reminder = newReminder()
        reminder.location = null
        val value = saveReminderViewModel.validateEnteredData(reminder)
        val snackBarValue = saveReminderViewModel.showSnackBarInt.getOrAwaitValue()
        assertThat(value, Matchers.`is`(false))
        Assert.assertEquals(
            context.getString(snackBarValue),
            context.resources.getString(R.string.err_select_location)
        )
    }
}