package com.udacity.project4.locationreminders.data.local

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
import androidx.test.platform.app.InstrumentationRegistry
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.data.dto.Result
import com.udacity.project4.util.newReminderDTO
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.CoreMatchers.instanceOf
import org.hamcrest.MatcherAssert.assertThat
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
//Medium Test to test the repository
@MediumTest
class RemindersLocalRepositoryTest {

    private lateinit var remindersDatabase: RemindersDatabase
    private lateinit var remindersDAO: RemindersDao
    private lateinit var remindersLocalRepository: RemindersLocalRepository

    @Before
    fun setupDatabase() {
        remindersDatabase = Room.inMemoryDatabaseBuilder(
            InstrumentationRegistry.getInstrumentation().context,
            RemindersDatabase::class.java
        ).allowMainThreadQueries()
         .build()

        remindersDAO = remindersDatabase.reminderDao()
        remindersLocalRepository = RemindersLocalRepository(remindersDAO)
    }

    @After
    fun closeDatabase() {
        remindersDatabase.close()
    }

    @Test
    fun getReminder_saveOneReminder_dataMatches() = runBlocking {
        val reminder = newReminderDTO()

        remindersLocalRepository.saveReminder(reminder)
        val result = remindersLocalRepository.getReminder(reminder.id)

        assertThat(result is Result.Success, `is`(true))
        result as Result.Success
        assertThat(result.data.title, `is`(reminder.title))
        assertThat(result.data.description, `is`(reminder.description))
        assertThat(result.data.location, `is`(reminder.location))
        assertThat(result.data.latitude, `is`(reminder.latitude))
        assertThat(result.data.longitude, `is`(reminder.longitude))
    }

    @Test
    fun getReminders_saveTwoReminders_correctSizeAndRemindersMatch() = runBlocking {
        val reminder1 = newReminderDTO()
        val reminder2 = newReminderDTO()
        remindersLocalRepository.saveReminder(reminder1)
        remindersLocalRepository.saveReminder(reminder2)

        val result = remindersLocalRepository.getReminders()
        assertThat(result is Result.Success, `is`(true))
        result as Result.Success
        val remindersResultList = result.data
        assertThat(remindersResultList.size, `is`(2))
        assertThat(remindersResultList.contains(reminder1), `is`(true))
        assertThat(remindersResultList.contains(reminder2), `is`(true))
    }

    @Test
    fun getReminders_saveTwoRemindersThenDeleteAllReminders_returnZeroSize() = runBlocking {
        val reminder1 = newReminderDTO()
        val reminder2 = newReminderDTO()
        remindersLocalRepository.saveReminder(reminder1)
        remindersLocalRepository.saveReminder(reminder2)

        remindersLocalRepository.deleteAllReminders()
        val result = remindersLocalRepository.getReminders()
        assertThat(result is Result.Success, `is`(true))
        result as Result.Success
        val remindersResultList = result.data
        assertThat(remindersResultList.size, `is`(0))
    }
}