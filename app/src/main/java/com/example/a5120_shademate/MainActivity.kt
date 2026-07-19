package com.example.a5120_shademate

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.mapbox.common.MapboxOptions
import com.example.a5120_shademate.ui.navigation.ShadeMateApp
import com.example.a5120_shademate.ui.screens.LaunchScreen
import com.example.a5120_shademate.ui.screens.ProfileCustomisationScreen
import com.example.a5120_shademate.ui.theme.ShadeMateTheme

private enum class LaunchDestination {
    ONBOARDING,
    CUSTOMISE,
    APP,
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        MapboxOptions.accessToken = getString(R.string.mapbox_access_token)
        setContent {
            ShadeMateTheme {
                Surface(color = MaterialTheme.colorScheme.background) {
                    var launchDestination by remember { mutableStateOf(LaunchDestination.ONBOARDING) }

                    when (launchDestination) {
                        LaunchDestination.ONBOARDING -> {
                            LaunchScreen(
                                onCustomize = { launchDestination = LaunchDestination.CUSTOMISE },
                                onSkip = { launchDestination = LaunchDestination.APP },
                            )
                        }

                        LaunchDestination.CUSTOMISE -> {
                            ProfileCustomisationScreen(
                                onBack = { launchDestination = LaunchDestination.APP },
                                showBackButton = false,
                                saveButtonText = "Save And Start",
                            )
                        }

                        LaunchDestination.APP -> {
                            ShadeMateApp()
                        }
                    }
                }
            }
        }
    }
}


//import android.os.Bundle
//import androidx.activity.ComponentActivity
//import androidx.activity.compose.setContent
//import androidx.compose.foundation.layout.fillMaxSize
//import androidx.compose.material3.MaterialTheme
//import androidx.compose.material3.Surface
//import androidx.compose.ui.Modifier
//import com.example.a5120_shademate.ui.screens.MapViewScreen
//
//class MainActivity : ComponentActivity() {
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//
//        setContent {
//            // Use the app theme
//            MaterialTheme {
//                Surface(
//                    modifier = Modifier.fillMaxSize(),
//                    color = MaterialTheme.colorScheme.background
//                ) {
//                    // Render the earlier map composable
//                    MapViewScreen(modifier = Modifier.fillMaxSize())
//                }
//            }
//        }
//    }
//}
