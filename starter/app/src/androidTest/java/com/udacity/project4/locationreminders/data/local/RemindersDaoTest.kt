package com.udacity.project4.locationreminders.data.local

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.SmallTest
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import junit.framework.Assert.assertEquals
import junit.framework.Assert.assertNull
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
//Unit test the DAO
@SmallTest
class RemindersDaoTest {

    private lateinit var remindersDatabase: RemindersDatabase
    private lateinit var dao: RemindersDao

    // Test Input
    private val reminder = ReminderDTO("title", "desc", "loc", 0.0, 0.2)

    @Before
    fun setup() {
        remindersDatabase = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            RemindersDatabase::class.java
        ).allowMainThreadQueries().build()

        dao = remindersDatabase.reminderDao()
    }

    @After
    fun tearDown() = remindersDatabase.close()

    @Test
    fun testSaveAndGetReminder() = runBlocking {
        assertEquals(0, dao.getReminders().size)
        dao.saveReminder(reminder)
        assertEquals(1, dao.getReminders().size)
        val savedReminder = dao.getReminderById(reminder.id)
        assertEquals(reminder, savedReminder)
    }

    @Test
    fun testSaveExistingReminderWithValueChanged() = runBlocking {
        val newTitle = "modified title"
        dao.saveReminder(reminder)
        val modifiedReminder = reminder.copy().apply {
            title = newTitle
        }
        dao.saveReminder(modifiedReminder)

        val savedReminder = dao.getReminderById(reminder.id)
        assertEquals(newTitle, savedReminder?.title)
    }

    @Test
    fun testDeleteReminderWhileDbIsEmpty() = runBlocking {
        assertEquals(0, dao.getReminders().size)
        dao.deleteAllReminders()
        assertEquals(0, dao.getReminders().size)
    }

    @Test
    fun testDeleteNonExistanceReminderId() = runBlocking {
        dao.saveReminder(reminder)
        assertEquals(1, dao.getReminders().size)
        dao.deleteReminderById("invalid_id")
        assertEquals(1, dao.getReminders().size)
    }

    @Test
    fun testGetReminderWithInvalidId() = runBlocking {
        dao.saveReminder(reminder)
        val queriedReminder = dao.getReminderById("invalid_id")
        assertNull(queriedReminder)
    }
}