package com.example.japanmap

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.japanmap.presentation.designsystem.JapanMapTheme
import com.example.japanmap.presentation.navigation.AppNavHost

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val container = (application as JapanMapApplication).container
        setContent {
            JapanMapTheme {
                AppNavHost(container = container)
            }
        }
    }
}
