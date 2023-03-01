package com.udacity.project4.locationreminders.data.local


import android.app.Application
import android.content.Context
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Database
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.IdlingRegistry
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
import com.udacity.project4.MainCoroutineRule
import com.udacity.project4.locationreminders.data.ReminderDataSource
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.data.dto.Result
import com.udacity.project4.locationreminders.reminderslist.RemindersListViewModel
import com.udacity.project4.util.DataBindingIdlingResource
import com.udacity.project4.utils.EspressoIdlingResource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.cancel
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runBlockingTest
import kotlinx.coroutines.test.runTest
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.MatcherAssert.assertThat

import org.hamcrest.core.IsEqual
import org.junit.*
import org.junit.runner.RunWith
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.module
import org.koin.test.AutoCloseKoinTest
import org.koin.test.get
import java.util.concurrent.Executors
import java.util.function.Predicate.isEqual

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
@MediumTest
class RemindersLocalRepositoryTest {

//    TODO: Add testing implementation to the RemindersLocalRepository.kt

    private val reminder = ReminderDTO("Title1", "Description1", location = "location1", latitude = 20.01, longitude = 30.01)
    private val reminder2 = ReminderDTO("Title2", "Description2", location = "location2", latitude = 20.02, longitude = 30.02)
    private val reminder3 = ReminderDTO("Title3", "Description3", location = "location3", latitude = 20.03, longitude = 30.03)
    private val localReminder = listOf(reminder).sortedBy { it.id }

//    private lateinit var remindersDao: RemindersDao

    // Class under test
    private lateinit var reminderRepository: RemindersLocalRepository
    private lateinit var database : RemindersDatabase


    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()


    @Before
    fun createRepository() {

        database =Room.inMemoryDatabaseBuilder(ApplicationProvider.getApplicationContext(), RemindersDatabase::class.java).setTransactionExecutor(
            Executors.newSingleThreadExecutor())
            .allowMainThreadQueries()
            .build()
        reminderRepository = RemindersLocalRepository(database.reminderDao(),Dispatchers.Main)



    }

    @After
    fun clean() {
        database.close()
    }



    @Test
    fun getReminder_requestsAllTasksFromLocalDataSource() = runBlocking {

        reminderRepository.saveReminder(reminder)
        // When tasks are requested from the tasks repository
        val reminderTest = reminderRepository.getReminders() as Result.Success


        // Then tasks are loaded from the remote data source
        Assert.assertThat(reminderTest.data, IsEqual(localReminder))
    }

    @Test
    fun getReminderById_RequestReminderById() = runBlocking {
        reminderRepository.saveReminder(reminder2)

        val reminderTest = reminderRepository.getReminder(reminder2.id) as Result.Success

        Assert.assertThat(reminderTest.data, IsEqual(reminder2))

    }

    @Test
    fun getReminderById_throwException() = runBlocking {
        val reminderTest  = reminderRepository.getReminder(reminder2.id) as Result

        Assert.assertThat(reminderTest, `is`(Result.Error("Reminder not found!")))
    }

}