package com.chaddy50.morningcommute

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import com.chaddy50.morningcommute.ui.theme.MorningCommuteTheme
import com.chaddy50.morningcommute.view.homeScreen.HomeScreen
import com.chaddy50.morningcommute.viewModel.MorningCommuteViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val viewModel: MorningCommuteViewModel by viewModels()
        setContent {
            MorningCommuteTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Column(modifier = Modifier.padding(innerPadding)) {
                        HomeScreen.Content(viewModel)
                    }
                }
            }
        }
    }
}