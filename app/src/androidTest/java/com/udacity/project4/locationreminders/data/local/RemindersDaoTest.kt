package com.udacity.project4.locationreminders.data.local

import android.os.Build
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider.getApplicationContext
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.SmallTest;
import com.udacity.project4.locationreminders.data.dto.ReminderDTO

import org.junit.Before;
import org.junit.Rule;
import org.junit.runner.RunWith;

import kotlinx.coroutines.ExperimentalCoroutinesApi;
import kotlinx.coroutines.test.runBlockingTest
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.CoreMatchers.notNullValue
import org.hamcrest.MatcherAssert.assertThat
import org.junit.After
import org.junit.Test
import org.robolectric.annotation.Config

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
//Unit test the DAO
@SmallTest
class RemindersDaoTest {

    @get:Rule
    var instantTaskExecutorRule = InstantTaskExecutorRule()
    private lateinit var reminderListDatabase: RemindersDatabase

    @Before
    fun initDb() {
        reminderListDatabase = Room.inMemoryDatabaseBuilder(
            getApplicationContext(),
            RemindersDatabase::class.java
        ).build()
    }

    @After
    fun closeDb() = reminderListDatabase.close()

    private fun newReminderDTO(): ReminderDTO {
        return ReminderDTO(
            "Trash",
            "Take out the trash",
            "Home",
            10.00,
            5.00
        )
    }

    @Test
    fun getReminderById_validateFieldsAreCorrect() = runBlockingTest {
        val reminder = newReminderDTO()

        reminderListDatabase.reminderDao().saveReminder(reminder)
        val loaded = reminderListDatabase.reminderDao().getReminderById(reminder.id)

        assertThat<ReminderDTO>(loaded as ReminderDTO, notNullValue())
        assertThat(loaded.id, `is`(reminder.id))
        assertThat(loaded.title, `is`(reminder.title))
        assertThat(loaded.description, `is`(reminder.description))
        assertThat(loaded.latitude, `is`(reminder.latitude))
        assertThat(loaded.longitude, `is`(reminder.longitude))
    }

    @Test
    fun getReminders_deleteAllReminders_isEmpty() = runBlockingTest {
        reminderListDatabase.reminderDao().saveReminder(newReminderDTO())
        reminderListDatabase.reminderDao().deleteAllReminders()
        val loadedReminders = reminderListDatabase.reminderDao().getReminders()
        assertThat(loadedReminders.isEmpty(), `is`(true))
    }

    @Test
    fun getReminders_saveThreeReminders_returnsSizeThree() = runBlockingTest {
        reminderListDatabase.reminderDao().saveReminder(newReminderDTO())
        reminderListDatabase.reminderDao().saveReminder(newReminderDTO())
        reminderListDatabase.reminderDao().saveReminder(newReminderDTO())
        val loadedReminders = reminderListDatabase.reminderDao().getReminders()
        assertThat(loadedReminders.size, `is`(3))
    }
}