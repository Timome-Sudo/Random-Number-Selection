package com.timome.sjxh

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.timome.sjxh.ui.screen.AboutScreen
import com.timome.sjxh.ui.screen.ExtractionScreen
import com.timome.sjxh.ui.screen.InputScreen
import com.timome.sjxh.ui.theme.SjxhTheme
import com.timome.sjxh.ui.viewmodel.StudentNumberViewModel
import com.timome.sjxh.datastore.SettingsDataStore

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SjxhTheme {
                val settingsDataStore = SettingsDataStore(this)
                RandomStudentNumberApp(settingsDataStore)
            }
        }
    }
}

@Composable
fun RandomStudentNumberApp(settingsDataStore: SettingsDataStore) {
    val navController = rememberNavController()
    val viewModel = remember { StudentNumberViewModel(settingsDataStore = settingsDataStore) }
    
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = "input",
            modifier = Modifier.padding(innerPadding)
        ) {
            composable("input") { 
                InputScreen(
                    viewModel = viewModel,
                    onStart = { 
                        viewModel.saveSettings()
                        navController.navigate("extraction") 
                    },
                    onNavigateToAbout = { navController.navigate("about") }
                )
            }
            composable("extraction") {
                ExtractionScreen(
                    viewModel = viewModel,
                    onBackToInput = {
                        viewModel.saveSettings()
                        viewModel.reset()
                        navController.popBackStack()
                    }
                )
            }
            composable("about") {
                AboutScreen(
                    onBack = { navController.popBackStack() }
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun RandomStudentNumberAppPreview() {
    SjxhTheme {
        val settingsDataStore = SettingsDataStore(androidx.compose.ui.platform.LocalContext.current as android.app.Activity)
        RandomStudentNumberApp(settingsDataStore)
    }
}