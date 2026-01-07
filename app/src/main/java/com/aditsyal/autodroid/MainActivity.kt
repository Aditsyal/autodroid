package com.aditsyal.autodroid

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.lifecycle.lifecycleScope
import com.aditsyal.autodroid.presentation.navigation.AppNavGraph
import com.aditsyal.autodroid.presentation.theme.AutodroidTheme
import com.aditsyal.autodroid.utils.MemoryMonitor
import com.aditsyal.autodroid.utils.PerformanceMonitor
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var performanceMonitor: PerformanceMonitor

    @Inject
    lateinit var memoryMonitor: MemoryMonitor

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        lifecycleScope.launch {
            memoryMonitor.logMemoryUsage("MainActivity_Create")
        }

        setContent {
            AutodroidTheme {
                AppNavGraph(
                    modifier = Modifier.fillMaxSize(),
                    performanceMonitor = performanceMonitor
                )
            }
        }
    }
}