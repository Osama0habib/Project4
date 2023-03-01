package com.udacity.project4.locationreminders.reminderslist

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.udacity.project4.locationreminders.MainCoroutineRule
import com.udacity.project4.locationreminders.data.FakeDataSource
import com.udacity.project4.locationreminders.data.dto.ReminderDTO

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
import org.hamcrest.CoreMatchers
import org.junit.Assert
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
import com.udacity.project4.locationreminders.data.dto.Result
import com.udacity.project4.locationreminders.getOrAwaitValue
import kotlinx.coroutines.test.pauseDispatcher
import kotlinx.coroutines.test.resumeDispatcher

@RunWith(AndroidJUnit4::class)
@ExperimentalCoroutinesApi
class RemindersListViewModelTest : AutoCloseKoinTest(){




    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    private lateinit var reminderListViewModel: RemindersListViewModel

    private lateinit var fakeDataSource: FakeDataSource

    @get:Rule
    var mainCoroutineRule = MainCoroutineRule()

    @Before
    fun init() {
        stopKoin()

        val testModule = module {
            viewModel {
                RemindersListViewModel(
                    app  = ApplicationProvider.getApplicationContext(),
                    fakeDataSource
                )
            }
        }

        startKoin {
            modules(listOf(testModule))
        }
        fakeDataSource = FakeDataSource()



//        saveReminderViewModel = SaveReminderViewModel( getApplication(),fakeLocalRepository)
        reminderListViewModel = get()
    }
    //TODO: provide testing to the RemindersListViewModel and its live data objects


    @Test
    fun loadReminder_returnSuccess()= mainCoroutineRule.runBlockingTest{



        reminderListViewModel.loadReminders()

        Assert.assertThat(
            reminderListViewModel.remindersList.value,
            CoreMatchers.`is`(emptyList())
        )

    }

    @Test
    fun loadReminder_returnFailure() = mainCoroutineRule.runBlockingTest {
        fakeDataSource.setReturnError(true)
        mainCoroutineRule.pauseDispatcher()

        reminderListViewModel.loadReminders()
        mainCoroutineRule.resumeDispatcher()


        Assert.assertThat(
            reminderListViewModel.showSnackBar.getOrAwaitValue(),
            CoreMatchers.`is`("Test exception")
        )
    }

}