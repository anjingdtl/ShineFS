package com.shinefs.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import com.shinefs.app.ui.compass.CompassScreen
import com.shinefs.app.ui.home.HomeScreen
import com.shinefs.app.ui.nav.Dest
import com.shinefs.app.ui.nav.Router

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ShineApp()
        }
    }
}

@Composable
fun ShineApp() {
    val router = remember { Router() }
    router.HandleBack()
    when (router.currentAsState()) {
        Dest.Home -> HomeScreen(onOpenCompass = { router.push(Dest.Compass) })
        Dest.Compass -> CompassScreen(onBack = { router.pop() })
    }
}
