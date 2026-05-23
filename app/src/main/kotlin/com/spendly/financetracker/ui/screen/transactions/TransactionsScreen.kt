package com.spendly.financetracker.ui.screen.transactions

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SmallFloatingActionButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.hilt.navigation.compose.hiltViewModel
import com.spendly.financetracker.data.model.FinanceTransaction
import com.spendly.financetracker.ui.components.NoRecordsState
import com.spendly.financetracker.ui.components.TransactionListItem
import com.spendly.financetracker.ui.navigation.Screen
import com.spendly.financetracker.ui.theme.SpendlyGray100
import com.spendly.financetracker.ui.theme.SpendlyGray300
import com.spendly.financetracker.ui.theme.SpendlyGray500
import com.spendly.financetracker.ui.theme.SpendlyGray700
import com.spendly.financetracker.ui.theme.SpendlyGreen
import com.spendly.financetracker.ui.util.currentMonthLabel
import com.spendly.financetracker.ui.util.formatDateFull
import com.spendly.financetracker.ui.viewmodel.TransactionTab
import com.spendly.financetracker.ui.viewmodel.TransactionsViewModel

typealias OnTransactionTabSelected = (TransactionTab) -> Unit

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun TransactionsScreen(
    navController: NavController
) {
    val viewModel: TransactionsViewModel = hiltViewModel()
    val state by viewModel.uiState.collectAsState()

    val groupedTransactions = state.filtered.groupBy { formatDateFull(it.createdAtMillis) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Events", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold) },
                actions = {
                    Surface(
                        color = SpendlyGray100,
                        shape = RoundedCornerShape(20.dp),
                        modifier = Modifier.padding(end = 16.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.CalendarMonth, null, tint = SpendlyGray700, modifier = Modifier.size(14.dp))
                            Spacer(Modifier.size(4.dp))
                            Text(currentMonthLabel(), style = MaterialTheme.typography.labelMedium, color = SpendlyGray700)
                        }
                    }
                }
            )
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            Column(modifier = Modifier.fillMaxSize()) {
                Column(
                    modifier = Modifier.fillMaxWidth().background(Color.White).padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        TransactionTab.values().forEach { tab ->
                            FilterChip(
                                selected = state.filter == tab,
                                onClick = { viewModel.setFilter(tab) },
                                label = { Text(tab.title) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = SpendlyGreen,
                                    selectedLabelColor = Color.White
                                )
                            )
                        }
                    }
                    Text(
                        "${state.filtered.size} transactions",
                        style = MaterialTheme.typography.labelSmall,
                        color = SpendlyGray500
                    )
                }

                HorizontalDivider(thickness = 0.5.dp, color = SpendlyGray300)

                if (state.filtered.isEmpty()) {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        when (state.filter) {
                            TransactionTab.ALL -> NoRecordsState()
                            TransactionTab.EXPENSES -> NoRecordsState(
                                actionLabel = "Add expense",
                                onAction = { navController.navigate(Screen.AddExpense.route) }
                            )
                            TransactionTab.INCOMES -> NoRecordsState(
                                actionLabel = "Add income",
                                onAction = { navController.navigate(Screen.AddIncome.route) }
                            )
                        }
                    }
                } else {
                    LazyColumn(modifier = Modifier.fillMaxSize()) {
                        groupedTransactions.forEach { (dateLabel, transactions) ->
                            stickyHeader {
                                Box(
                                    modifier = Modifier.fillMaxWidth().background(SpendlyGray100).padding(horizontal = 16.dp, vertical = 6.dp)
                                ) {
                                    Text(
                                        "$dateLabel - ${transactions.size}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = SpendlyGray500,
                                        letterSpacing = 0.5.sp
                                    )
                                }
                            }
                            items(items = transactions, key = FinanceTransaction::id) { transaction ->
                                TransactionListItem(
                                    transaction = transaction,
                                    showContainer = false,
                                    onDelete = { viewModel.delete(transaction) }
                                )
                                HorizontalDivider(thickness = 0.5.dp, color = SpendlyGray100)
                            }
                        }
                    }
                }
            }

            // FAB with Income/Expense expand
            var fabExpanded by remember { mutableStateOf(false) }
            Column(
                modifier = Modifier.align(Alignment.BottomEnd).padding(end = 16.dp, bottom = 24.dp),
                horizontalAlignment = Alignment.End
            ) {
                if (fabExpanded) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("Income", style = MaterialTheme.typography.labelMedium, color = SpendlyGray700, modifier = Modifier.padding(end = 8.dp))
                        Spacer(Modifier.size(8.dp))
                        SmallFloatingActionButton(
                            onClick = { fabExpanded = false; navController.navigate(Screen.AddIncome.route) },
                            containerColor = SpendlyGreen, shape = CircleShape
                        ) { Icon(Icons.Default.Add, contentDescription = "Add income", tint = Color.White) }
                    }
                    Spacer(Modifier.size(8.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("Expense", style = MaterialTheme.typography.labelMedium, color = SpendlyGray700, modifier = Modifier.padding(end = 8.dp))
                        Spacer(Modifier.size(8.dp))
                        SmallFloatingActionButton(
                            onClick = { fabExpanded = false; navController.navigate(Screen.AddExpense.route) },
                            containerColor = SpendlyGreen, shape = CircleShape
                        ) { Icon(Icons.Default.Add, contentDescription = "Add expense", tint = Color.White) }
                    }
                    Spacer(Modifier.size(8.dp))
                }
                FloatingActionButton(
                    onClick = { fabExpanded = !fabExpanded },
                    containerColor = SpendlyGreen, shape = CircleShape
                ) { Icon(Icons.Default.Add, contentDescription = "Add") }
            }
        }
    }
}
