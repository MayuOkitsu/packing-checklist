package com.mochimono.packingchecklist

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.navigation.compose.rememberNavController
import com.mochimono.packingchecklist.ui.navigation.PackingNavHost
import com.mochimono.packingchecklist.ui.theme.PackingChecklistTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            PackingChecklistTheme {
                PackingNavHost(navController = rememberNavController())
            }
        }
    }
}
