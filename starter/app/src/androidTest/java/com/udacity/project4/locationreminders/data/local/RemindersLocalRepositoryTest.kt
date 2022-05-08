package com.udacity.project4.locationreminders.data.local

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.data.dto.Result
import junit.framework.Assert.assertEquals
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
//Medium Test to test the repository
@MediumTest
class RemindersLocalRepositoryTest {

    private lateinit var remindersDatabase: RemindersDatabase
    private lateinit var remindersLocalRepository: RemindersLocalRepository
    private lateinit var dao: RemindersDao

    // Test Input
    private val reminder = ReminderDTO("title", "desc", "loc", 0.0, 0.2)
    private val reminder1 = ReminderDTO("title", "desc", "loc", 0.0, 0.2)
    private val ERROR_ITEM_NOT_FOUND = "Reminder not found!"

    @Before
    fun setup() {
        remindersDatabase = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            RemindersDatabase::class.java
        ).allowMainThreadQueries().build()

        dao = remindersDatabase.reminderDao()

        remindersLocalRepository = RemindersLocalRepository(dao, Dispatchers.Main)
    }

    @After
    fun tearDown() = remindersDatabase.close()


    @Test
    fun saveReminder_retrieveReminder() = runBlocking {
        remindersLocalRepository.saveReminder(reminder)

        val result = remindersLocalRepository.getReminder(reminder.id) as Result.Success

        assertEquals(result.data, reminder)
    }


    @Test
    fun getNonExistanceReminder() = runBlocking {
        remindersLocalRepository.saveReminder(reminder)

        val result = remindersLocalRepository.getReminder("invalid") as Result.Error

        assertEquals(ERROR_ITEM_NOT_FOUND, result.message)
    }

    @Test
    fun deleteAllReminder() = runBlocking {
        remindersLocalRepository.saveReminder(reminder)
        remindersLocalRepository.deleteAllReminders()

        val result = remindersLocalRepository.getReminder(reminder.id) as Result.Error

        assertEquals(ERROR_ITEM_NOT_FOUND, result.message)
    }

    @Test
    fun getReminders() = runBlocking {
        remindersLocalRepository.apply {
            saveReminder(reminder)
            saveReminder(reminder1)
        }

        val result = remindersLocalRepository.getReminders() as Result.Success
        assertEquals(2, result.data.size)
    }
}