package com.example.staysafe.viewModel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.example.staysafe.model.database.StaySafeDatabase

class StaySafeViewModel(application: Application) : AndroidViewModel(application) {
    private val db = StaySafeDatabase.getDatabase(application)
    val userDao = db.userDao()
    val activityDao = db.activityDao()
}
