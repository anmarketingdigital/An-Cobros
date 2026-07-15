package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.lifecycle.ViewModelProvider
import com.example.data.database.AppDatabase
import com.example.data.repository.MarketingRepository
import com.example.ui.screens.MainScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.MarketingViewModel

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    
    // Initialize Database & Repository
    val database = AppDatabase.getDatabase(applicationContext)
    val repository = MarketingRepository(database.clientLeadDao(), database.meetingDao())
    
    // Initialize ViewModel using the Custom Factory
    val viewModel = ViewModelProvider(
        this,
        MarketingViewModel.Factory(repository)
    )[MarketingViewModel::class.java]

    enableEdgeToEdge()
    setContent {
      MyApplicationTheme {
        Surface(
          modifier = Modifier.fillMaxSize()
        ) {
          MainScreen(viewModel = viewModel)
        }
      }
    }
  }
}
