package com.udacity.project4.locationreminders.geofence

import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.ContentValues.TAG
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofenceStatusCodes
import com.google.android.gms.location.GeofencingEvent
import com.udacity.project4.MyApp
import com.udacity.project4.R
import com.udacity.project4.locationreminders.RemindersActivity
import com.udacity.project4.locationreminders.data.ReminderDataSource
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.data.dto.Result
import com.udacity.project4.locationreminders.data.local.LocalDB
import com.udacity.project4.locationreminders.data.local.RemindersLocalRepository
import com.udacity.project4.locationreminders.reminderslist.ReminderDataItem
import com.udacity.project4.locationreminders.reminderslist.RemindersListViewModel
import com.udacity.project4.utils.sendNotification
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import org.koin.android.ext.android.get
import org.koin.android.ext.android.inject
import org.koin.core.Koin
import org.koin.core.KoinComponent
import org.koin.core.context.GlobalContext.get
import org.koin.core.inject
import org.koin.core.module.Module
import org.koin.dsl.module
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.coroutineContext

/**
 * Triggered by the Geofence.  Since we can have many Geofences at once, we pull the request
 * ID from the first Geofence, and locate it within the cached data in our Room DB
 *
 * Or users can add the reminders and then close the app, So our app has to run in the background
 * and handle the geofencing in the background.
 * To do that you can use https://developer.android.com/reference/android/support/v4/app/JobIntentService to do that.
 *
 */

fun errorMessage(context: Context, errorCode: Int): String {
    val resources = context.resources
    return when (errorCode) {
        GeofenceStatusCodes.GEOFENCE_NOT_AVAILABLE -> resources.getString(
            R.string.geofence_not_available
        )
        GeofenceStatusCodes.GEOFENCE_TOO_MANY_GEOFENCES -> resources.getString(
            R.string.geofence_too_many_geofences
        )
        GeofenceStatusCodes.GEOFENCE_TOO_MANY_PENDING_INTENTS -> resources.getString(
            R.string.geofence_too_many_pending_intents
        )
        else -> resources.getString(R.string.error_happened)
    }
}


class GeofenceBroadcastReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        println("onrecive called")


//TODO: implement the onReceive method to receive the geofencing events at the background
        val geofencingEvent = GeofencingEvent.fromIntent(intent)
        if (geofencingEvent.hasError()) {
            val errorMessage = errorMessage(context, geofencingEvent.errorCode)
            Log.e(TAG, errorMessage)
            return
        }

        if (geofencingEvent.geofenceTransition == Geofence.GEOFENCE_TRANSITION_ENTER) {
            val fenceId = when {
                geofencingEvent.triggeringGeofences.isNotEmpty() ->
                    geofencingEvent.triggeringGeofences.forEach {
                        val koinHelper = KoinHelper()
                        koinHelper.getReminder(it.requestId,context)
                    }
                else -> {
                    Log.e(TAG, "No Geofence Trigger Found! Abort mission!")
                    return
                }
            }




//            val dataSource : RemindersLocalRepository = get()


        }


    }
}


class KoinHelper : KoinComponent{
    val reminderDataSource: ReminderDataSource by inject()
    fun getReminder( id: String,context: Context) {
        CoroutineScope(Dispatchers.IO).launch {
            val result: Result<ReminderDTO> = reminderDataSource.getReminder(id)
            if (result is Result.Success<ReminderDTO>) {
                val reminder = result.data
                val reminderDataItem = ReminderDataItem(
                    title = reminder.title,
                    description = reminder.description,
                    location = reminder.location,
                    latitude = reminder.latitude,
                    longitude = reminder.longitude,
                    id = reminder.id
                )
                sendNotification(context, reminderDataItem = reminderDataItem)

            }
        }
    }
}

