package com.udacity.project4.locationreminders.base

import android.app.Application
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.Observer
import io.mockk.MockKAnnotations
import io.mockk.impl.annotations.MockK
import io.mockk.mockkObject
import io.mockk.unmockkAll
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Rule

abstract class BaseViewModelTest {
    @MockK
    protected lateinit var mockApplication: Application

    @MockK(relaxed = true)
    protected lateinit var showToastObserver: Observer<String>

    @MockK(relaxed = true)
    protected lateinit var showLoadingObserver: Observer<Boolean>


    @Rule
    @JvmField
    internal val ruleForLiveData = InstantTaskExecutorRule()

    @Before
    open fun setUp() {
        MockKAnnotations.init(this) //Makes @MockK annotation work
        mockkObject()
        // for coroutine test
        Dispatchers.setMain(Dispatchers.Default)
    }

    @After
    open fun tearDown() {
        unmockkAll()
    }
}