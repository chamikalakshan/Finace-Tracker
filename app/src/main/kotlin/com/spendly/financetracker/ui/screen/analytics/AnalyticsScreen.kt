package com.spendly.financetracker.ui.screen.analytics

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.spendly.financetracker.data.model.TransactionType
import com.spendly.financetracker.ui.components.NoAnalyticsState
import com.spendly.financetracker.ui.theme.ChartColors
import com.spendly.financetracker.ui.theme.ChartPurple
import com.spendly.financetracker.ui.theme.SpendlyAmber
import com.spendly.financetracker.ui.theme.SpendlyGray300
import com.spendly.financetracker.ui.theme.SpendlyGray500
import com.spendly.financetracker.ui.theme.SpendlyGray700
import com.spendly.financetracker.ui.theme.SpendlyGray100
import com.spendly.financetracker.ui.theme.SpendlyGreen
import com.spendly.financetracker.ui.theme.SpendlyGreenDark
import com.spendly.financetracker.ui.theme.SpendlyGreenLight
import com.spendly.financetracker.ui.theme.SpendlyRed
import com.spendly.financetracker.ui.theme.SpendlyRedDark
import com.spendly.financetracker.ui.theme.SpendlyRedLight
import com.spendly.financetracker.ui.util.currentMonthLabel
import com.spendly.financetracker.ui.util.formatMoney
import com.spendly.financetracker.ui.viewmodel.FinanceUiState
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
fun AnalyticsScreen(state: FinanceUiState) {
    val totalIncome = state.incomeCents
    val totalExpense = state.expenseCents

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Analytics",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                },
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
                            Spacer(Modifier.width(4.dp))
                            Text(
                                currentMonthLabel(),
                                style = MaterialTheme.typography.labelMedium,
                                color = SpendlyGray700
                            )
                        }
                    }
                }
            )
        }
    ) { padding ->
        if (totalIncome == 0L && totalExpense == 0L) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                NoAnalyticsState()
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        SummaryTile(
                            label = "Total Income",
                            value = formatMoney(totalIncome),
                            background = SpendlyGreenLight,
                            textColor = SpendlyGreenDark,
                            modifier = Modifier.weight(1f)
                        )
                        SummaryTile(
                            label = "Total Expenses",
                            value = formatMoney(totalExpense),
                            background = SpendlyRedLight,
                            textColor = SpendlyRedDark,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                item {
                    SpendingByCategoryCard(state = state)
                }

                item {
                    SpendingSplitCard(state = state)
                }

                item {
                    MonthlyOverviewCard(state = state)
                }

                item {
                    IncomeSourcesCard(state = state)
                }

                item { Spacer(Modifier.height(80.dp)) }
            }
        }
    }
}

@Composable
private fun SummaryTile(
    label: String,
    value: String,
    background: Color,
    textColor: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = background),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(label, style = MaterialTheme.typography.labelSmall, color = textColor)
            Text(value, style = MaterialTheme.typography.titleMedium, color = textColor, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun SpendingByCategoryCard(state: FinanceUiState) {
    val categories = state.transactions
        .filter { it.type == TransactionType.EXPENSE }
        .groupBy { it.category.ifBlank { "Other" }.take(18) }
        .mapValues { entry -> entry.value.sumOf { it.amountCents } }
        .toList()
        .sortedByDescending { it.second }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(0.5.dp, SpendlyGray300),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text("Spending by Category", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(170.dp),
                contentAlignment = Alignment.Center
            ) {
                CategoryRing(categories = categories, total = state.expenseCents)
                Text(
                    text = "Spent\n${formatMoney(state.expenseCents)}",
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.labelMedium
                )
            }

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                categories.take(5).forEachIndexed { index, (category, amount) ->
                    val percent = if (state.expenseCents > 0) ((amount * 100) / state.expenseCents).toInt() else 0
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(10.dp)
                                .clip(CircleShape)
                                .background(ChartColors[index % ChartColors.size])
                        )
                        Text(category, style = MaterialTheme.typography.bodySmall, modifier = Modifier.weight(1f))
                        Text("$percent%", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, color = SpendlyGray700)
                    }
                }
            }
        }
    }
}

@Composable
private fun CategoryRing(categories: List<Pair<String, Long>>, total: Long) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(5.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        categories.take(7).forEachIndexed { index, (_, amount) ->
            val percent = if (total > 0L) amount.toFloat() / total.toFloat() else 0f
            Box(
                modifier = Modifier
                    .width(16.dp)
                    .height((36.dp + 90.dp * percent).coerceAtLeast(36.dp))
                    .clip(RoundedCornerShape(10.dp))
                    .background(ChartColors[index % ChartColors.size])
            )
        }
    }
}

@Composable
private fun SpendingSplitCard(state: FinanceUiState) {
    val expenseCents = state.expenseCents
    val committedCategories = setOf("Rent", "Subscriptions")
    val committed = state.transactions
        .filter { it.type == TransactionType.EXPENSE && it.category in committedCategories }
        .sumOf { it.amountCents }
    val discretionary = (expenseCents - committed).coerceAtLeast(0L)

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(0.5.dp, SpendlyGray300),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text("Committed vs Discretionary", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
            Text(
                "Your spending split - committed costs cannot easily be reduced.",
                style = MaterialTheme.typography.labelSmall,
                color = SpendlyGray500
            )
            SplitBar("Committed", committed, expenseCents, ChartPurple, "Rent, subscriptions")
            SplitBar("Discretionary", discretionary, expenseCents, SpendlyAmber, "Food, transport, other")
        }
    }
}

@Composable
private fun SplitBar(
    label: String,
    amount: Long,
    total: Long,
    color: Color,
    subtitle: String
) {
    val percent = if (total > 0L) ((amount * 100) / total).toInt() else 0
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Row(verticalAlignment = Alignment.Bottom) {
            Column(modifier = Modifier.weight(1f)) {
                Text(label, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                Text(subtitle, style = MaterialTheme.typography.labelSmall, color = SpendlyGray500)
            }
            Text("${formatMoney(amount)} ($percent%)", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
        }
        LinearProgressIndicator(
            progress = { percent / 100f },
            modifier = Modifier
                .fillMaxWidth()
                .height(10.dp)
                .clip(RoundedCornerShape(5.dp)),
            color = color,
            trackColor = color.copy(alpha = 0.15f)
        )
    }
}

@Composable
private fun MonthlyOverviewCard(state: FinanceUiState) {
    val data = lastFiveMonths().map { (label, month, year) ->
            val transactions = state.transactions.filter {
            val calendar = Calendar.getInstance().apply { timeInMillis = it.dateMillis }
            calendar.get(Calendar.MONTH) == month && calendar.get(Calendar.YEAR) == year
        }
        MonthOverview(
            label = label,
            income = transactions.filter { it.type == TransactionType.INCOME }.sumOf { it.amountCents },
            expense = transactions.filter { it.type == TransactionType.EXPENSE }.sumOf { it.amountCents }
        )
    }
    val maxValue = data.maxOfOrNull { maxOf(it.income, it.expense) }?.coerceAtLeast(1L) ?: 1L

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(0.5.dp, SpendlyGray300),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text("Monthly Overview (5 months)", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.align(Alignment.End)) {
                LegendItem("Income", SpendlyGreen)
                LegendItem("Expenses", SpendlyRed)
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.Bottom
            ) {
                data.forEach { item ->
                    Column(
                        modifier = Modifier.weight(1f),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Bottom
                    ) {
                        Row(
                            modifier = Modifier
                                .height(140.dp)
                                .fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            verticalAlignment = Alignment.Bottom
                        ) {
                            ChartBar(item.income, maxValue, SpendlyGreen, Modifier.weight(1f))
                            ChartBar(item.expense, maxValue, SpendlyRed, Modifier.weight(1f))
                        }
                        Spacer(Modifier.height(8.dp))
                        Text(item.label, style = MaterialTheme.typography.labelSmall, color = SpendlyGray500)
                    }
                }
            }
        }
    }
}

@Composable
private fun ChartBar(value: Long, maxValue: Long, color: Color, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxHeight()
            .background(color.copy(alpha = 0.12f), RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp)),
        contentAlignment = Alignment.BottomCenter
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight((value.toFloat() / maxValue.toFloat()).coerceIn(0.04f, 1f))
                .background(color, RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp))
        )
    }
}

@Composable
private fun IncomeSourcesCard(state: FinanceUiState) {
    val sources = state.transactions
        .filter { it.type == TransactionType.INCOME }
        .groupBy { it.source.ifBlank { "Other" }.take(18) }
        .mapValues { entry -> entry.value.sumOf { it.amountCents } }
        .toList()
        .sortedByDescending { it.second }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(0.5.dp, SpendlyGray300),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text("Income Sources - ${currentMonthLabel().substringBefore(" ")}", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
            sources.forEachIndexed { index, (source, amount) ->
                val color = ChartColors[index % ChartColors.size]
                val percent = if (state.incomeCents > 0) ((amount * 100) / state.incomeCents).toInt() else 0
                Column(verticalArrangement = Arrangement.spacedBy(4.dp), modifier = Modifier.padding(bottom = 8.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(source, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                        Text(formatMoney(amount), style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                    }
                    LinearProgressIndicator(
                        progress = { percent / 100f },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp)),
                        color = color,
                        trackColor = color.copy(alpha = 0.15f)
                    )
                    Text("$percent% of total income", style = MaterialTheme.typography.labelSmall, color = SpendlyGray500)
                }
            }
        }
    }
}

@Composable
private fun LegendItem(label: String, color: Color) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        Box(
            modifier = Modifier
                .size(10.dp)
                .clip(CircleShape)
                .background(color)
        )
        Text(label, style = MaterialTheme.typography.labelSmall, color = SpendlyGray500)
    }
}

private data class MonthOverview(
    val label: String,
    val income: Long,
    val expense: Long
)

private fun lastFiveMonths(): List<Triple<String, Int, Int>> {
    val formatter = SimpleDateFormat("MMM", Locale.getDefault())
    val calendar = Calendar.getInstance()
    return (4 downTo 0).map { offset ->
        val monthCalendar = calendar.clone() as Calendar
        monthCalendar.add(Calendar.MONTH, -offset)
        Triple(
            formatter.format(Date(monthCalendar.timeInMillis)),
            monthCalendar.get(Calendar.MONTH),
            monthCalendar.get(Calendar.YEAR)
        )
    }
}
