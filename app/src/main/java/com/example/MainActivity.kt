package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.screens.DailyHubApp
import com.example.ui.theme.DailyHubTheme
import com.example.ui.viewmodel.DailyHubViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            DailyHubTheme {
                val viewModel: DailyHubViewModel = viewModel()
                DailyHubApp(viewModel = viewModel)
            }
        }
    }
}
