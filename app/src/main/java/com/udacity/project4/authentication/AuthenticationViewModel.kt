package com.udacity.project4.authentication

import android.app.Application
import androidx.lifecycle.map
import com.udacity.project4.base.BaseViewModel

class AuthenticationViewModel(app: Application): BaseViewModel(app) {

    val userIsAuthenticated = FirebaseUserLiveData().map { user ->
        user != null
    }
}