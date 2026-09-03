package br.com.sensorauto

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import br.com.sensorauto.navigation.SensorAutoNavHost
import br.com.sensorauto.ui.theme.SensorAutoTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SensorAutoTheme {
                SensorAutoNavHost()
            }
        }
    }
}
