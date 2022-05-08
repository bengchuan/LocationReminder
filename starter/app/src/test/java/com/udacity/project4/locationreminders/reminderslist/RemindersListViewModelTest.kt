package com.udacity.project4.locationreminders.reminderslist

import androidx.lifecycle.Observer
import com.udacity.project4.locationreminders.base.BaseViewModelTest
import com.udacity.project4.locationreminders.data.ErrorMessage
import com.udacity.project4.locationreminders.data.FakeDataSource
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import io.mockk.impl.annotations.MockK
import io.mockk.verify
import junit.framework.Assert.assertEquals
import kotlinx.coroutines.runBlocking
import org.junit.Test

class RemindersListViewModelTest : BaseViewModelTest() {

    @MockK(relaxed = true)
    private lateinit var showNoDataObserver: Observer<Boolean>

    @MockK(relaxed = true)
    private lateinit var showSnackbarObserver: Observer<String?>

    private val testReminderDTO = ReminderDTO("title", null, null, null, null, "test")

    private lateinit var viewModel: RemindersListViewModel
    private lateinit var dataSource: FakeDataSource

    override fun setUp() {
        super.setUp()
        dataSource = FakeDataSource()
        viewModel = RemindersListViewModel(mockApplication, dataSource)
        viewModel.apply {
            showNoData.observeForever(showNoDataObserver)
            showLoading.observeForever(showLoadingObserver)
            showSnackBar.observeForever(showSnackbarObserver)
        }
    }

    @Test
    fun `test when ReminderList is empty, showNoData is triggered`() {
        viewModel.loadReminders()
        verify(exactly = 1) { showLoadingObserver.onChanged(true) }
        verify(exactly = 1) { showNoDataObserver.onChanged(true) }
        assertEquals(0, viewModel.remindersList.value?.size)
    }

    @Test
    fun `test when ReminderList is non-empty, showNoData is not triggered`() = runBlocking {

        dataSource.saveReminder(testReminderDTO)
        viewModel.loadReminders()

        verify(exactly = 0) { showNoDataObserver.onChanged(true) }
        assertEquals(1, viewModel.remindersList.value?.size)
    }

    @Test
    fun `test when datasource returns error upon loadReminder`() = runBlocking {
        dataSource.saveReminder(testReminderDTO)
        dataSource.shouldReturnError = true

        viewModel.loadReminders()

        verify(exactly = 1) { showNoDataObserver.onChanged(true) }
        verify(exactly = 1) { showSnackbarObserver.onChanged(ErrorMessage.ERROR_MSG_GET_REMINDERS.message) }
    }

}