package com.chaddy50.morningcommute

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.ui.Modifier
import com.chaddy50.morningcommute.ui.theme.MorningCommuteTheme
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.util.Locale
import com.chaddy50.morningcommute.view.homeScreen.HomeScreen
import com.chaddy50.morningcommute.viewModel.MorningCommuteViewModel

class MainActivity : ComponentActivity() {
    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val viewModel: MorningCommuteViewModel by viewModels()
        setContent {
            MorningCommuteTheme {
                Scaffold(
                    topBar = {
                        val locale = Locale.getDefault()
                        val commuteDate = viewModel.commuteDate
                        val dayName = commuteDate.format(DateTimeFormatter.ofPattern("EEEE", locale))
                        val date = commuteDate.format(DateTimeFormatter.ofLocalizedDate(FormatStyle.SHORT).withLocale(locale))
                        TopAppBar({ Text("$dayName, $date Commute") })
                    },
                    modifier = Modifier.fillMaxSize(),
                ) { innerPadding ->
                    Column(
                        modifier = Modifier.padding(innerPadding),
                        verticalArrangement = Arrangement.Top
                    ) {
                        HomeScreen.Content(viewModel)
                    }
                }
            }
        }
    }
}