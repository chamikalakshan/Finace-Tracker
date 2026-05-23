package com.spendly.financetracker

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.hilt.navigation.compose.hiltViewModel
import com.spendly.financetracker.ui.FinanceTrackerApp
import com.spendly.financetracker.ui.theme.FinanceTrackerTheme
import com.spendly.financetracker.ui.viewmodel.FinanceViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val viewModel: FinanceViewModel = hiltViewModel()

            FinanceTrackerTheme {
                FinanceTrackerApp(viewModel = viewModel)
            }
        }
    }
}
