package com.example.pepsi

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.pepsi.navigation.PepsiNavHost
import com.example.pepsi.theme.PepsiTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            PepsiTheme {
                PepsiNavHost()
            }
        }
    }
}
