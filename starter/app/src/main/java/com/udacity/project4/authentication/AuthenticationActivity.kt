package com.udacity.project4.authentication

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import com.firebase.ui.auth.AuthUI
import com.udacity.project4.R
import com.udacity.project4.locationreminders.RemindersActivity
import kotlinx.android.synthetic.main.activity_authentication.*
import org.koin.androidx.viewmodel.ext.android.viewModel

/**
 * This class should be the starting point of the app, It asks the users to sign in / register, and redirects the
 * signed in users to the RemindersActivity.
 */
class AuthenticationActivity : AppCompatActivity() {

    private val viewModel: AuthenticationViewModel by viewModel()

    companion object {
        const val SIGN_IN_REQUEST_CODE = 1000
        const val TAG = "AuthenticationActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_authentication)
        auth_button.setOnClickListener {
            launchSignInFlow()
        }
        viewModel.authenticated.observe(this, Observer {
            if (it) {
                Log.i(TAG, "User is authenticated, start RemindersActivity")
                val intent = Intent(this, RemindersActivity::class.java)
                startActivity(intent)
            }
        })
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == SIGN_IN_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                Log.i(TAG, "Login successful")
                // Set Firebase Current User

                //Navigate to Location Reminder
            } else {
                Log.w(TAG, "Login failed.")
            }
        }
    }


    private fun launchSignInFlow() {
        val providers = arrayListOf(
            AuthUI.IdpConfig.EmailBuilder().build(),
            AuthUI.IdpConfig.GoogleBuilder().build()
        )

        startActivityForResult(
            AuthUI.getInstance()
                .createSignInIntentBuilder()
                .setAvailableProviders(providers)
                .build(),
            SIGN_IN_REQUEST_CODE
        )
    }
}
