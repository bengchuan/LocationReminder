package com.udacity.project4.authentication

import android.app.Application
import androidx.lifecycle.Transformations
import com.udacity.project4.locationreminders.base.BaseViewModel

class AuthenticationViewModel(app: Application) : BaseViewModel(app) {
    val authenticated = Transformations.map(FirebaseUserLiveData()) {
        it != null
    }


}