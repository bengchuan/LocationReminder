package com.udacity.project4.locationreminders.reminderslist

import android.os.Bundle
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.test.core.app.ApplicationProvider.getApplicationContext
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
import com.udacity.project4.R
import com.udacity.project4.locationreminders.data.FakeDataSource
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.savereminder.SaveReminderViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.module
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify


@RunWith(AndroidJUnit4::class)
@ExperimentalCoroutinesApi
//UI Testing
@MediumTest
class ReminderListFragmentTest {

    private lateinit var fakeDataSource: FakeDataSource
    private val testReminderDTOs = listOf(
        ReminderDTO("Test Reminder 1", "Test Description1", "Test Location1", 0.1, 0.1, "1"),
        ReminderDTO("Test Reminder 2", "Test Description2", "Test Location2", 0.2, 0.2, "2"),
        ReminderDTO("Test Reminder 3", "Test Description3", "Test Location3", 0.3, 0.3, "3")
    )

    @Before
    fun setup() {
        stopKoin()
        // initialise fakedata source
        fakeDataSource = FakeDataSource()

        // initialize viewmodel
        // See: https://insert-koin.io/docs/reference/koin-android/instrumented-testing
        val instrumentedTestModule = module {
            viewModel { RemindersListViewModel(get(), fakeDataSource) }
            viewModel { SaveReminderViewModel(get(), fakeDataSource) }
        }

        startKoin {
            androidContext(getApplicationContext())
            modules(listOf(instrumentedTestModule))
        }
    }


    @Test
    fun emptyDataSource_showEmptyList() {
        // Launching the fragment.
        launchFragmentInContainer<ReminderListFragment>(Bundle(), R.style.AppTheme)

        onView(withText(R.string.no_data)).check(matches(isDisplayed()))
        onView(withId(R.id.noDataTextView)).check(matches(isDisplayed()))
    }

    @Test
    fun validRemindersExist_showSavedItemsInTheList() = runBlockingTest {
        testReminderDTOs.forEach {
            fakeDataSource.saveReminder(it)
        }

        // Launching the fragment.
        launchFragmentInContainer<ReminderListFragment>(Bundle(), R.style.AppTheme)
        testReminderDTOs.forEach {
            onView(withText(it.title)).check(matches(isDisplayed()))
            onView(withText(it.description)).check(matches(isDisplayed()))
        }
    }

    @Test
    fun clickFAB_navigateToReminderFragment() = runBlockingTest {
        // Launching the fragment.
        val scenario = launchFragmentInContainer<ReminderListFragment>(Bundle(), R.style.AppTheme)

        // /Add a mock navController
        val mockNavController = mock(NavController::class.java)
        scenario.onFragment { fragment ->
            fragment.view?.let {
                Navigation.setViewNavController(it, mockNavController)
            }
        }

        onView(withId(R.id.addReminderFAB)).perform(click())
        verify(mockNavController).navigate(ReminderListFragmentDirections.toSaveReminder())
    }
}