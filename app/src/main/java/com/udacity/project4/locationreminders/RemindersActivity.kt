package com.udacity.project4.locationreminders

import android.Manifest
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentContainerView
import androidx.navigation.fragment.NavHostFragment
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.udacity.project4.R
import com.udacity.project4.authentication.AuthenticationActivity
//import kotlinx.android.synthetic.main.activity_reminders.*

/**
 * The RemindersActivity that holds the reminders fragments
 */
class RemindersActivity : AppCompatActivity() {

    private lateinit var requestPermissionLauncher: ActivityResultLauncher<String>
    private val notificationManager: NotificationManager by lazy {
        getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_reminders)

        requestNotificationPermission()

    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    fun requestNotificationPermission(){
        requestPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) {
//            if (it) {
//                showDummyNotification()
//            } else {
//                Snackbar.make(
//                    findViewById<View>(android.R.id.content).rootView,
//                    "Please grant Notification permission from App Settings",
//                    Snackbar.LENGTH_LONG
//                ).show()
//            }
        }
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.POST_NOTIFICATIONS,
            ) == PackageManager.PERMISSION_GRANTED
        ) {
        } else {
            requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        println(item.title)
        when (item.itemId) {
            R.id.home -> {
                (findViewById<FragmentContainerView>(R.id.nav_host_fragment) as NavHostFragment).navController.popBackStack()
                return true
            }
            R.id.logout -> {
                FirebaseAuth.getInstance().signOut()
                val signInIntent = Intent(this,AuthenticationActivity::class.java)
                startActivity(signInIntent)
            }


        }
        return super.onOptionsItemSelected(item)
    }
}
