package com.example.staysafe

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.example.staysafe.map.MapScreen
import com.example.staysafe.model.database.StaySafeDatabase
import androidx.lifecycle.lifecycleScope
import com.example.staysafe.model.data.*
import com.example.staysafe.model.dummyData.DatabaseSeeder
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val db = StaySafeDatabase.getDatabase(this)
        lifecycleScope.launch {
            DatabaseSeeder.insertDummyData(db)
        }

        setContent {
            MapScreen()
        }
    }
}
