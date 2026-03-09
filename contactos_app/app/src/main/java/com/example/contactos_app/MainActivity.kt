package com.example.contactos_app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.*
import com.example.contactos_app.ui.theme.Contactos_appTheme
import com.example.contactos_app.viewmodel.ContactViewModel
import com.example.contactos_app.screens.*

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            Contactos_appTheme {
                val navController = rememberNavController()
                val viewModel: ContactViewModel = viewModel()

                NavHost(
                    navController = navController,
                    startDestination = "list"
                ) {
                    composable("list") {
                        ListScreen(navController, viewModel)
                    }

                    composable("detail/{id}") { backStackEntry ->
                        val id = backStackEntry.arguments
                            ?.getString("id")
                            ?.toInt() ?: 0
                        DetailScreen(navController, viewModel, id)
                    }

                    composable("form") {
                        FormScreen(navController, viewModel, null)
                    }

                    composable("form/{id}") { backStackEntry ->
                        val id = backStackEntry.arguments
                            ?.getString("id")
                            ?.toInt()
                        FormScreen(navController, viewModel, id)
                    }
                }
            }
        }
    }
}
