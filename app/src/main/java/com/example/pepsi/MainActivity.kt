package com.example.pepsi

import android.content.pm.ApplicationInfo
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.pepsi.auth.AuthSession
import com.example.pepsi.navigation.AppRoot
import com.example.pepsi.network.RetrofitClient
import com.example.pepsi.theme.PepsiTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        RetrofitClient.logBodies = (applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE) != 0
        AuthSession.init(applicationContext)
        setContent {
            PepsiTheme {
                AppRoot()
            }
        }
    }
}
