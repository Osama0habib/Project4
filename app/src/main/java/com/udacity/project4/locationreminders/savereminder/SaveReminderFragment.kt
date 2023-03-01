package com.udacity.project4.locationreminders.savereminder

import android.Manifest
import android.annotation.SuppressLint
import android.annotation.TargetApi
import android.app.PendingIntent
import android.content.ContentValues.TAG
import android.content.Intent
import android.content.IntentSender
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*
import com.google.android.material.snackbar.Snackbar
import com.udacity.project4.BuildConfig
import com.udacity.project4.R
import com.udacity.project4.base.BaseFragment
import com.udacity.project4.base.NavigationCommand
import com.udacity.project4.databinding.FragmentSaveReminderBinding
import com.udacity.project4.locationreminders.geofence.GeofenceBroadcastReceiver
import com.udacity.project4.locationreminders.reminderslist.ReminderDataItem
import com.udacity.project4.utils.setDisplayHomeAsUpEnabled
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.sharedViewModel


class SaveReminderFragment : BaseFragment() {
    //Get the view model this time as a single to be shared with the another fragment
    override val _viewModel: SaveReminderViewModel by sharedViewModel()
    private lateinit var binding: FragmentSaveReminderBinding
    lateinit var geofencingClient: GeofencingClient
    private lateinit var reminderData : ReminderDataItem
    private val runningQOrLater = android.os.Build.VERSION.SDK_INT >=
            android.os.Build.VERSION_CODES.Q




    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        binding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_save_reminder, container, false)

        setDisplayHomeAsUpEnabled(true)
        geofencingClient = LocationServices.getGeofencingClient(this.requireActivity())

        binding.viewModel = _viewModel

        binding.lifecycleOwner = this

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.lifecycleOwner = this
        binding.selectLocation.setOnClickListener {
            //            Navigate to another fragment to get the user location
            _viewModel.navigationCommand.value =
                NavigationCommand.To(SaveReminderFragmentDirections.actionSaveReminderFragmentToSelectLocationFragment())
        }

        binding.saveReminder.setOnClickListener {
            val title = _viewModel.reminderTitle.value
            val description = _viewModel.reminderDescription.value
            val location = _viewModel.reminderSelectedLocationStr.value
            val latitude = _viewModel.latitude.value
            val longitude = _viewModel.longitude.value
            reminderData = ReminderDataItem(title =title, description = description , latitude = latitude, longitude = longitude , location = location)
            requestForegroundAndBackgroundLocationPermissions()

//            TODO: use the user entered reminder details to:

//             1) add a geofencing request
//             2) save the reminder to the local db
        }
    }

    @TargetApi(29)
    private fun foregroundAndBackgroundLocationPermissionApproved(): Boolean {
        val foregroundLocationApproved = (
                PackageManager.PERMISSION_GRANTED ==
                        ContextCompat.checkSelfPermission(this.requireContext(),
                            Manifest.permission.ACCESS_FINE_LOCATION))
        val backgroundPermissionApproved =
            if (runningQOrLater) {
                PackageManager.PERMISSION_GRANTED ==
                        ContextCompat.checkSelfPermission(
                            this.requireActivity(), Manifest.permission.ACCESS_BACKGROUND_LOCATION
                        )
            } else {
                true
            }
        return foregroundLocationApproved && backgroundPermissionApproved
    }
    @TargetApi(29 )
    private fun requestForegroundAndBackgroundLocationPermissions() {
        println("request foreground")
        if (foregroundAndBackgroundLocationPermissionApproved()) {
            checkDeviceLocationSettingsAndStartGeofence(true)
            return
        }
        var permissionsArray = arrayOf(Manifest.permission.ACCESS_FINE_LOCATION)
        val resultCode = when {
            runningQOrLater -> {
                permissionsArray += Manifest.permission.ACCESS_BACKGROUND_LOCATION
                REQUEST_FOREGROUND_AND_BACKGROUND_PERMISSION_RESULT_CODE
            }
            else -> REQUEST_FOREGROUND_ONLY_PERMISSIONS_REQUEST_CODE
        }
        Log.d(TAG, "Request foreground only location permission")
        requestPermissions(
            permissionsArray,
            resultCode
        )
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray,
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
//        Log.d(TAG, "onRequestPermissionResult")
        println("onrequestPermissionResult")
        println(permissions[0])
        println(grantResults)
        if (
            grantResults.isEmpty() ||
            grantResults[LOCATION_PERMISSION_INDEX] == PackageManager.PERMISSION_DENIED ||
            (requestCode == REQUEST_FOREGROUND_AND_BACKGROUND_PERMISSION_RESULT_CODE &&
                    grantResults[BACKGROUND_LOCATION_PERMISSION_INDEX] ==
                    PackageManager.PERMISSION_DENIED))
        {
            Snackbar.make(
                binding.saveReminderMain,
                R.string.permission_denied_explanation,
                Snackbar.LENGTH_INDEFINITE
            )
                .setAction(R.string.settings) {
                    startActivity(Intent().apply {
                        action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
                        data = Uri.fromParts("package", BuildConfig.APPLICATION_ID, null)
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    })
                }.show()
        } else {
            println("permission granted")
            checkDeviceLocationSettingsAndStartGeofence(true)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        //make sure to clear the view model after destroy, as it's a single view model.
        _viewModel.onClear()
    }

    @SuppressLint("MissingPermission")
    private fun checkDeviceLocationSettingsAndStartGeofence(resolve:Boolean ) {
        println("checkDevice")
        val locationRequest = LocationRequest.create().apply {
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        }
        val builder = LocationSettingsRequest.Builder().addLocationRequest(locationRequest)
        val settingsClient = LocationServices.getSettingsClient(this.requireActivity())
        val locationSettingsResponseTask =
            settingsClient.checkLocationSettings(builder.build())
        locationSettingsResponseTask.addOnFailureListener { exception ->
            if (exception is ResolvableApiException && resolve){
                try {
//                    exception.startResolutionForResult(this.requireActivity(),
//                        REQUEST_TURN_DEVICE_LOCATION_ON)
                    this.startIntentSenderForResult(exception.getResolution().getIntentSender(),REQUEST_TURN_DEVICE_LOCATION_ON, null, 0, 0, 0, null)
                } catch (sendEx: IntentSender.SendIntentException) {
                    Log.d(TAG, "Error getting location settings resolution: " + sendEx.message)
                }
            } else {
                Snackbar.make(
                    binding.saveReminderMain,
                    R.string.location_required_error, Snackbar.LENGTH_INDEFINITE
                ).setAction(android.R.string.ok) {
                    checkDeviceLocationSettingsAndStartGeofence(true)
                }.show()
            }
        }
        locationSettingsResponseTask.addOnCompleteListener {
            if ( it.isSuccessful ) {
                val geofencingRequest = _viewModel.buildGeoFencing(reminderData)
                 val geofencePendingIntent: PendingIntent by lazy {
                     val intent = Intent(_viewModel.app, GeofenceBroadcastReceiver::class.java)
                     if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                          PendingIntent.getBroadcast(
                             _viewModel.app,
                             0,
                             intent,
                             PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE
                         )
                     } else {
                          PendingIntent.getBroadcast(
                             _viewModel.app,
                             0,
                             intent,
                             PendingIntent.FLAG_UPDATE_CURRENT
                         )
                     }
//                    PendingIntent.getBroadcast(_viewModel.app, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)
                }
                geofencingClient.addGeofences(geofencingRequest,geofencePendingIntent).run {
                    addOnSuccessListener {
                        Log.d(TAG, "Location added!!!")
                        _viewModel.validateAndSaveReminder(reminderData)
                        // save reminder to local db
                    }
                    addOnFailureListener {
                        Toast.makeText(context, "Failed to add location!!! Try again later!", Toast.LENGTH_SHORT).show()
                        println("geo fences fails")
                    }
                }

            }
        }
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        println("onActivity Result")
        if (requestCode == REQUEST_TURN_DEVICE_LOCATION_ON) {
            checkDeviceLocationSettingsAndStartGeofence(true)
        }
    }


}
private const val REQUEST_FOREGROUND_AND_BACKGROUND_PERMISSION_RESULT_CODE = 33
private const val REQUEST_FOREGROUND_ONLY_PERMISSIONS_REQUEST_CODE = 34
private const val REQUEST_TURN_DEVICE_LOCATION_ON = 29
//private const val TAG = "HuntMainActivity"
private const val LOCATION_PERMISSION_INDEX = 0
private const val BACKGROUND_LOCATION_PERMISSION_INDEX = 1