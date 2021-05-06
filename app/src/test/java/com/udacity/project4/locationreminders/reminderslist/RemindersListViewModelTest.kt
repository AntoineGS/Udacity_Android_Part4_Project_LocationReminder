package com.udacity.project4.locationreminders.reminderslist

import android.app.Application
import android.os.Build
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.udacity.project4.locationreminders.data.FakeDataSource
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.getOrAwaitValue
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestCoroutineDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runBlockingTest
import kotlinx.coroutines.test.setMain
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.core.context.stopKoin
import org.robolectric.annotation.Config

@Config(sdk = [Build.VERSION_CODES.Q])
@RunWith(AndroidJUnit4::class)
@ExperimentalCoroutinesApi
class RemindersListViewModelTest {

    private lateinit var fakeDataSource: FakeDataSource
    private lateinit var context: Application
    private lateinit var remindersListViewModel: RemindersListViewModel
    private lateinit var remindersDTO: MutableList<ReminderDTO>

    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    @ExperimentalCoroutinesApi
    private val testDispatcher = TestCoroutineDispatcher()

    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
        remindersDTO = mutableListOf(
            ReminderDTO(
                "Trash",
                "Take out the trash",
                "Home",
                10.0,
                5.0
            )
        )
        fakeDataSource = FakeDataSource(remindersDTO)
        remindersListViewModel = RemindersListViewModel(context, fakeDataSource)
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        stopKoin()
        Dispatchers.resetMain()
        testDispatcher.cleanupTestCoroutines()
    }

    @Test
    fun loadReminders_remindersList_notEmpty() = runBlockingTest {
        remindersListViewModel.loadReminders()
        val remindersList = remindersListViewModel.remindersList.getOrAwaitValue()
        assertThat(remindersList.isNotEmpty(), Matchers.`is`(true))
    }

    @Test
    fun loadReminders_showNoData_returnsFalse() = runBlockingTest {
        remindersListViewModel.loadReminders()
        val showNoData = remindersListViewModel.showNoData.getOrAwaitValue()
        assertThat(showNoData, Matchers.`is`(false))
    }

    @Test
    fun loadReminders_showLoading_startsTrueThenFalseOnceLoaded() = runBlockingTest {
        testDispatcher.pauseDispatcher()
        remindersListViewModel.loadReminders()
        val showLoadingValue = remindersListViewModel.showLoading.getOrAwaitValue()
        assertThat(showLoadingValue, Matchers.`is`(true))
        testDispatcher.resumeDispatcher()
        val showLoadingValueAgain = remindersListViewModel.showLoading.getOrAwaitValue()
        assertThat(showLoadingValueAgain, Matchers.`is`(false))
    }

    @Test
    fun deleteReminders_showSnackBar_shouldReturnError() = runBlockingTest {
        remindersListViewModel.loadReminders()
        val remindersList = remindersListViewModel.remindersList.getOrAwaitValue()
        assertThat(remindersList.isEmpty(), Matchers.`is`(false))
        fakeDataSource.deleteAllReminders()
        remindersListViewModel.loadReminders()
        val noRemindersMessage = remindersListViewModel.showSnackBar.getOrAwaitValue()
        assertThat(noRemindersMessage, Matchers.`is`("No reminders found"))
    }
}