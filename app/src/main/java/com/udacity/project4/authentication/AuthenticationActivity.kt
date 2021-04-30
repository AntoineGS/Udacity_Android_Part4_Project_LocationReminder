package com.udacity.project4.authentication

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.firebase.ui.auth.AuthUI
import com.udacity.project4.R
import com.udacity.project4.databinding.ActivityAuthenticationBinding
import com.udacity.project4.locationreminders.RemindersActivity

/**
 * This class should be the starting point of the app, It asks the users to sign in / register, and redirects the
 * signed in users to the RemindersActivity.
 */
class AuthenticationActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAuthenticationBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityAuthenticationBinding.inflate(layoutInflater)
        binding.viewModel = ViewModelProvider(this).get(AuthenticationViewModel::class.java)
        setContentView(binding.root)

        binding.viewModel?.userIsAuthenticated?.observe(this, {
            if (it) {
                val intent = Intent(this, RemindersActivity::class.java)
                startActivity(intent)
            }
        })

        binding.loginButton.setOnClickListener {
            requestLogin()
        }
    }

    private fun requestLogin() {
        val providers = arrayListOf(
            AuthUI.IdpConfig.EmailBuilder().build(), AuthUI.IdpConfig.GoogleBuilder().build()
        )

        startActivity(
            AuthUI.getInstance()
                .createSignInIntentBuilder()
                .setLogo(R.drawable.map)
                .setIsSmartLockEnabled(false)
                .setAvailableProviders(providers)
                .build()
        )
    }
}
