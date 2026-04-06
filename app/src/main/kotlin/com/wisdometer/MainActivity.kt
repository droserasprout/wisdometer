// app/src/main/kotlin/com/wisdometer/MainActivity.kt
package com.wisdometer

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.wisdometer.ui.navigation.NavGraph
import com.wisdometer.ui.theme.WisdometerTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            WisdometerTheme {
                NavGraph()
            }
        }
    }
}
