package com.chaddy50.morningcommute.view

import androidx.compose.runtime.Composable
import com.chaddy50.morningcommute.viewModel.MorningCommuteViewModel

interface Screen {
    val route: String

    @Composable
    fun Content(
        viewModel: MorningCommuteViewModel
    )
}