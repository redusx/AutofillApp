package com.example.autofillapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.autofillapp.data.AppDatabase
import com.example.autofillapp.data.ProfileRepository
import com.example.autofillapp.security.CryptoManager
import com.example.autofillapp.ui.MainNavigation
import com.example.autofillapp.ui.ProfileViewModel
import com.example.autofillapp.ui.theme.AutofillAppTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val cryptoManager = CryptoManager()
        val database = AppDatabase.getInstance(applicationContext)
        val repository = ProfileRepository(database.userProfileDao(), cryptoManager)

        setContent {
            AutofillAppTheme {
                val profileViewModel: ProfileViewModel =
                        viewModel(factory = ProfileViewModel.Factory(repository))
                MainNavigation(viewModel = profileViewModel)
            }
        }
    }
}
