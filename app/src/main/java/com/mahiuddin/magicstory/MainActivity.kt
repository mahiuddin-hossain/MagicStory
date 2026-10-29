package com.mahiuddin.magicstory

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.mahiuddin.magicstory.navigation.MagicStoryNavGraph
import com.mahiuddin.magicstory.ui.theme.MagicStoryTheme
import com.mahiuddin.magicstory.ui.theme.MainBackground

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MagicStoryTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MainBackground
                ) {
                    MagicStoryNavGraph()
                }
            }
        }
    }
}
