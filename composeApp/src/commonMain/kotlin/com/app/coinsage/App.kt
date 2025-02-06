package com.app.coinsage

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import com.app.coinsage.presentations.Dashboard
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
@Preview
fun App() {
    MaterialTheme {
        Dashboard()
    }
}