package com.example.staysafe

import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.RequiresApi
import com.example.staysafe.model.database.StaySafeDatabase
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.staysafe.model.dummyData.DatabaseSeeder
import com.example.staysafe.nav.Navigation
import com.example.staysafe.viewModel.MapViewModel
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val db = StaySafeDatabase.getDatabase(this)
        lifecycleScope.launch {
            DatabaseSeeder.insertDummyData(db)
        }

        setContent {
            Navigation(database = db)
        }
    }
}
