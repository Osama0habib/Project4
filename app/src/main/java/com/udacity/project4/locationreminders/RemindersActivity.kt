package com.udacity.project4.locationreminders

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentContainerView
import androidx.navigation.fragment.NavHostFragment
import com.google.firebase.auth.FirebaseAuth
import com.udacity.project4.R
import com.udacity.project4.authentication.AuthenticationActivity
//import kotlinx.android.synthetic.main.activity_reminders.*

/**
 * The RemindersActivity that holds the reminders fragments
 */
class RemindersActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_reminders)


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
