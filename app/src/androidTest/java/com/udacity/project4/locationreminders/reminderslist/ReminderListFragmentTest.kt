package com.udacity.project4.locationreminders.reminderslist

import android.app.Application
import android.os.Bundle
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.lifecycle.ViewModel
import androidx.navigation.NavController
import androidx.navigation.Navigation

import androidx.test.core.app.ApplicationProvider.getApplicationContext
import androidx.test.espresso.Espresso
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.IdlingRegistry
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
import com.udacity.project4.R
import com.udacity.project4.locationreminders.data.ReminderDataSource
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.data.local.LocalDB
import com.udacity.project4.locationreminders.data.local.RemindersDao
import com.udacity.project4.locationreminders.data.local.RemindersDaoTest
import com.udacity.project4.locationreminders.data.local.RemindersLocalRepository
import com.udacity.project4.locationreminders.savereminder.SaveReminderViewModel
import com.udacity.project4.util.DataBindingIdlingResource
import com.udacity.project4.util.monitorActivity
import com.udacity.project4.util.monitorFragment
import com.udacity.project4.utils.EspressoIdlingResource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runBlockingTest
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.module

import org.koin.dsl.ScopeSet

import org.koin.test.AutoCloseKoinTest
import org.koin.test.get
import org.mockito.ArgumentMatchers
import org.mockito.Mockito
import org.mockito.Mockito.verify


@ExperimentalCoroutinesApi
@MediumTest
@RunWith(AndroidJUnit4::class)
class ReminderListFragmentTest : AutoCloseKoinTest() {

    private lateinit var repository: ReminderDataSource
    private lateinit var appContext: Application
    private val dataBindingIdlingResource = DataBindingIdlingResource()

    @Before
    fun initRepository() {
        stopKoin()
        appContext = getApplicationContext()
        val myModule = module {
            viewModel {
                RemindersListViewModel(appContext,get() as ReminderDataSource)
            }
            single {
                //This view model is declared singleton to be used across multiple fragments
                RemindersLocalRepository(get()) as ReminderDataSource
            }
            single {
                LocalDB.createRemindersDao(appContext)
            }
        }

        startKoin {
            modules(listOf(myModule))
        }
        repository = get()

        runBlocking {
            repository.deleteAllReminders()
        }
    }



    @Before
    fun registerIdlingResource(){
        IdlingRegistry.getInstance().register(EspressoIdlingResource.countingIdlingResource)
        IdlingRegistry.getInstance().register(dataBindingIdlingResource)
    }

    @After
    fun unRegisterIdlingResource(){
        IdlingRegistry.getInstance().unregister(EspressoIdlingResource.countingIdlingResource)
        IdlingRegistry.getInstance().unregister(dataBindingIdlingResource)
    }

//    TODO: test the navigation of the fragments.

    @Test
    fun addReminder_navigateTo_saveReminder(){

        val scenario = launchFragmentInContainer<ReminderListFragment>(Bundle(), R.style.AppTheme)
            dataBindingIdlingResource.monitorFragment(scenario)

        val navController = Mockito.mock(NavController::class.java)
        scenario.onFragment {
            Navigation.setViewNavController(it.view!!, navController)
        }
        onView(withId(R.id.addReminderFAB))
            .perform(click())

        verify(navController).navigate(ReminderListFragmentDirections.toSaveReminder())
    }
//    TODO: test the displayed data on the UI.

    @Test
    fun addReminder_checkReminderListUi() {
        val reminder = ReminderDTO(title = "title", description = "description", location = "location", latitude = 20.0, longitude = 20.0)
        runBlocking{
            repository.saveReminder(reminder)
        }

        val scenario = launchFragmentInContainer<ReminderListFragment>(Bundle(), R.style.AppTheme)
        dataBindingIdlingResource.monitorFragment(scenario)


        onView(withText(reminder.title)).check(matches(isDisplayed()))
        onView(withText(reminder.description)).check(matches(isDisplayed()))
        onView(withText(reminder.location)).check(matches(isDisplayed()))

    }
//    TODO: add testing for the error messages.

    @Test
    fun showReminder_showNoData(){
        val scenario = launchFragmentInContainer<ReminderListFragment>(Bundle(), R.style.AppTheme)
        dataBindingIdlingResource.monitorFragment(scenario)

        onView(withId(R.id.noDataTextView)).check(matches(isDisplayed()))
    }
}