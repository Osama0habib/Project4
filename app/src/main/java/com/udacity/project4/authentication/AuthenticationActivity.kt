package com.udacity.project4.authentication

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.firebase.ui.auth.AuthUI
import com.firebase.ui.auth.AuthUI.IdpConfig.EmailBuilder
import com.firebase.ui.auth.AuthUI.IdpConfig.GoogleBuilder
import com.firebase.ui.auth.FirebaseAuthUIActivityResultContract
import com.firebase.ui.auth.data.model.FirebaseAuthUIAuthenticationResult
import com.google.firebase.auth.FirebaseAuth
import com.udacity.project4.R
import com.udacity.project4.locationreminders.RemindersActivity



/**
 * This class should be the starting point of the app, It asks the users to sign in / register, and redirects the
 * signed in users to the RemindersActivity.
 */
class AuthenticationActivity : AppCompatActivity() {
    var auth = FirebaseAuth.getInstance()

    private val signInLauncher = registerForActivityResult(
        FirebaseAuthUIActivityResultContract()
    ) { result: FirebaseAuthUIAuthenticationResult? ->
        if (auth.currentUser != null) {
            val homeIntent = Intent(this, RemindersActivity::class.java)
            startActivity(homeIntent)
        }
    }

// ...

    // ...
    private fun startSignIn() {
        val signInIntent = AuthUI.getInstance()
            .createSignInIntentBuilder()
            .setAvailableProviders(
                listOf(
                    GoogleBuilder().build(),

                    EmailBuilder().build(),
                )
            )
            .build()
        signInLauncher.launch(signInIntent)
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

//         TODO: Implement the create account and sign in using FirebaseUI, use sign in using email and sign in using Google
        if (auth.currentUser != null) {
            val homeIntent = Intent(this,RemindersActivity::class.java)
            startActivity(homeIntent)
        } else {
            setContentView(R.layout.activity_authentication)
            findViewById<Button>(R.id.login_button).setOnClickListener {
                startSignIn()
            }
            // not signed in
        }



//          TODO: If the user was authenticated, send him to RemindersActivity

//          TODO: a bonus is to customize the sign in flow to look nice using :
        //https://github.com/firebase/FirebaseUI-Android/blob/master/auth/README.md#custom-layout

    }
}
