package com.udacity.project4.locationreminders.savereminder

import android.app.Application
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.udacity.project4.R
import com.udacity.project4.base.NavigationCommand
import com.udacity.project4.locationreminders.MainCoroutineRule
import com.udacity.project4.locationreminders.data.FakeDataSource
import com.udacity.project4.locationreminders.data.ReminderDataSource
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.data.local.RemindersLocalRepository
import com.udacity.project4.locationreminders.getOrAwaitValue
import com.udacity.project4.locationreminders.reminderslist.ReminderDataItem
import org.hamcrest.CoreMatchers.`is`
import org.junit.Assert.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.pauseDispatcher
import kotlinx.coroutines.test.resumeDispatcher
import kotlinx.coroutines.test.runBlockingTest
import org.hamcrest.CoreMatchers
import org.hamcrest.CoreMatchers.not
import org.junit.Before
import org.junit.Rule
import org.junit.Test

import org.junit.runner.RunWith
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.module
import org.koin.test.AutoCloseKoinTest

import org.koin.test.get
import org.robolectric.RuntimeEnvironment.getApplication


@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
class SaveReminderViewModelTest : AutoCloseKoinTest(){


    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    private lateinit var saveReminderViewModel: SaveReminderViewModel

    private lateinit var FakeDataSource: FakeDataSource

    @get:Rule
    var mainCoroutineRule = MainCoroutineRule()

    @Before
    fun init() {
        stopKoin()

        val testModule = module {
            viewModel {
                SaveReminderViewModel(
                    app  = ApplicationProvider.getApplicationContext(),
                    FakeDataSource
                )
            }
        }

        startKoin {
            modules(listOf(testModule))
        }
        FakeDataSource = FakeDataSource()



//        saveReminderViewModel = SaveReminderViewModel( getApplication(),fakeLocalRepository)
        saveReminderViewModel = get()
    }

//    @After
//    fun tearDown() {
//        stopKoin()
//    }

    @Test
    fun saveTask_showLoading() = mainCoroutineRule.runBlockingTest {
        val reminder = ReminderDataItem("Title", "Description","location",30.00,30.00)
        mainCoroutineRule.pauseDispatcher()
        saveReminderViewModel.validateAndSaveReminder(reminder)

        assertThat(saveReminderViewModel.showLoading.getOrAwaitValue(),`is`(true))


        mainCoroutineRule.resumeDispatcher()

        assertThat(saveReminderViewModel.showLoading.getOrAwaitValue(),`is`(false))

    }

    @Test
    fun validateData_shouldReturnError(){
        val reminder = ReminderDataItem("Title", "Description",null,30.00,30.00)
        val result = saveReminderViewModel.validateEnteredData(reminder)


        assertThat(result,`is`(false))
    }

    @Test
    fun saveReminder_showToast_navigate() = mainCoroutineRule.runBlockingTest{
        val reminder = ReminderDataItem("Title", "Description","location",30.00,30.00)
        saveReminderViewModel.validateAndSaveReminder(reminder)

        assertThat(saveReminderViewModel.showToast.getOrAwaitValue(), `is`(getApplication().getString(R.string.reminder_saved)))
        assertThat(saveReminderViewModel.navigationCommand.getOrAwaitValue(), `is`(NavigationCommand.Back))
    }



    //TODO: provide testing to the SaveReminderView and its live data objects




}