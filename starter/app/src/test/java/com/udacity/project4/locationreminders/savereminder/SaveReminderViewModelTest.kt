package com.udacity.project4.locationreminders.savereminder

import androidx.lifecycle.Observer
import com.google.android.gms.maps.model.PointOfInterest
import com.udacity.project4.R
import com.udacity.project4.locationreminders.base.BaseViewModelTest
import com.udacity.project4.locationreminders.data.FakeDataSource
import com.udacity.project4.locationreminders.reminderslist.ReminderDataItem
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.verify
import junit.framework.Assert.assertTrue
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runBlockingTest
import org.junit.Assert.assertFalse
import org.junit.Test

class SaveReminderViewModelTest : BaseViewModelTest() {

    //could have used getString() method. Since it is only English-only,
    // leave it as it is.
    private val RESOURCE_STRING_REMINDER_SAVED = "reminder saved"


    private lateinit var viewModel: SaveReminderViewModel

    // Note: with mockk, we don't really need FakeDataSource
    // However, we will using it here to fulfil the assignment requirements.
    private lateinit var dataSource: FakeDataSource

    @MockK(relaxed = true)
    private lateinit var titleObserver: Observer<String>

    @MockK(relaxed = true)
    private lateinit var descObserver: Observer<String>

    @MockK(relaxed = true)
    private lateinit var selectedLocationObserver: Observer<String>

    @MockK(relaxed = true)
    private lateinit var longtitudeObserver: Observer<Double>

    @MockK(relaxed = true)
    private lateinit var latitudeObserver: Observer<Double>

    @MockK(relaxed = true)
    private lateinit var selectedPOIObserver: Observer<PointOfInterest>

    @MockK(relaxed = true)
    private lateinit var mockReminderDataItem: ReminderDataItem


    override fun setUp() {
        super.setUp()
        dataSource = FakeDataSource()
        viewModel = SaveReminderViewModel(mockApplication, dataSource)

        // respond the R.strings
        every { mockApplication.getString(R.string.reminder_saved) } returns RESOURCE_STRING_REMINDER_SAVED

        // Bind observers to viewmodel's livedata
        viewModel.apply {
            reminderTitle.observeForever(titleObserver)
            reminderDescription.observeForever(descObserver)
            reminderSelectedLocationStr.observeForever(selectedLocationObserver)
            longitude.observeForever(longtitudeObserver)
            latitude.observeForever(latitudeObserver)
            selectedPOI.observeForever(selectedPOIObserver)
            showToast.observeForever(showToastObserver)
            showLoading.observeForever(showLoadingObserver)
        }
    }

    @Test
    fun `test when onClear called, all livedata are reset to null`() {
        // onClear
        viewModel.onClear()

        verify(exactly = 1) { titleObserver.onChanged(null) }
        verify(exactly = 1) { descObserver.onChanged(null) }
        verify(exactly = 1) { selectedLocationObserver.onChanged(null) }
        verify(exactly = 1) { longtitudeObserver.onChanged(null) }
        verify(exactly = 1) { latitudeObserver.onChanged(null) }
        verify(exactly = 1) { selectedPOIObserver.onChanged(null) }
    }

    @ExperimentalCoroutinesApi
    @Test
    fun `test saveReminder will able to save item without error`() = runBlockingTest {
        launch {
            viewModel.saveReminder(mockReminderDataItem)
            verify(exactly = 1) { showToastObserver.onChanged(RESOURCE_STRING_REMINDER_SAVED) }
            verify(exactly = 1) { showLoadingObserver.onChanged(false) }
        }
    }

    @Test
    fun `test validateEnteredData will return true if title and location is not null`() {
        // making reminderDataItem title and location not null
        every { mockReminderDataItem.title }.answers { "title" }
        every { mockReminderDataItem.location }.answers { "location" }

        val result = viewModel.validateEnteredData(mockReminderDataItem)
        assertTrue(result)
    }

    @Test
    fun `test validateEnteredData will return appropriate error if title is null`() {
        every { mockReminderDataItem.title }.answers { null }
        every { mockReminderDataItem.location }.answers { "location" }

        val result = viewModel.validateEnteredData(mockReminderDataItem)
        assertFalse(result)
    }

    @Test
    fun `test validateEnteredData will return appropriate error if location is null`() {
        every { mockReminderDataItem.title }.answers { "title" }
        every { mockReminderDataItem.location }.answers { null }

        val result = viewModel.validateEnteredData(mockReminderDataItem)
        assertFalse(result)
    }

}