package com.spendly.financetracker.ui.screen.transactions

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.hilt.navigation.compose.hiltViewModel
import com.spendly.financetracker.ui.theme.SpendlyGray300
import com.spendly.financetracker.ui.theme.SpendlyGray500
import com.spendly.financetracker.ui.theme.SpendlyGreen
import com.spendly.financetracker.ui.viewmodel.AddIncomeViewModel
import com.spendly.financetracker.ui.viewmodel.incomeSources
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun AddIncomeScreen(
    onBack: () -> Unit
) {
    val viewModel: AddIncomeViewModel = hiltViewModel()
    val state by viewModel.uiState.collectAsState()

    LaunchedEffect(state.isSaved) {
        if (state.isSaved) onBack()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                },
                title = { Text("Add Income") },
                actions = {
                    Button(
                        onClick = viewModel::save,
                        enabled = !state.isLoading,
                        colors = ButtonDefaults.buttonColors(containerColor = SpendlyGreen, contentColor = Color.White),
                        shape = RoundedCornerShape(20.dp),
                        modifier = Modifier.padding(end = 8.dp).height(36.dp),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 0.dp)
                    ) { Text("Save", style = MaterialTheme.typography.labelMedium) }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            if (state.isLoading) LinearProgressIndicator(modifier = Modifier.fillMaxWidth(), color = SpendlyGreen)
            if (state.error != null) {
                Text(state.error!!, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.labelMedium)
            }

            // Amount
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("Amount (LKR)", style = MaterialTheme.typography.labelSmall, color = SpendlyGray500, textAlign = TextAlign.Center)
                Box(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp), contentAlignment = Alignment.Center) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("LKR", style = MaterialTheme.typography.headlineMedium, color = SpendlyGreen, fontWeight = FontWeight.Bold)
                        Spacer(Modifier.width(4.dp))
                        BasicTextField(
                            value = state.amount,
                            onValueChange = viewModel::onAmountChanged,
                            textStyle = TextStyle(
                                fontSize = 48.sp, fontWeight = FontWeight.Bold,
                                color = if (state.amount.isEmpty()) SpendlyGray300 else SpendlyGreen,
                                textAlign = TextAlign.Center
                            ),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            decorationBox = { inner ->
                                if (state.amount.isEmpty()) Text("0", fontSize = 48.sp, color = SpendlyGray300, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
                                inner()
                            }
                        )
                    }
                }
                Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(SpendlyGreen))
            }

            // Source
            AddIncomeLabel("Income Source")
            FlowRow(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                incomeSources.forEach { source ->
                    FilterChip(
                        selected = state.selectedSource == source,
                        onClick = { viewModel.onSourceSelected(source) },
                        label = { Text(source) },
                        colors = FilterChipDefaults.filterChipColors(selectedContainerColor = SpendlyGreen, selectedLabelColor = Color.White)
                    )
                }
            }

            // Name
            AddIncomeLabel("Name")
            OutlinedTextField(
                value = state.name,
                onValueChange = viewModel::onNameChanged,
                placeholder = { Text("e.g. May Salary, Client ABC") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            // Date
            AddIncomeLabel("Date")
            var showDatePicker by remember { mutableStateOf(false) }
            OutlinedTextField(
                value = SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date(state.selectedDate)),
                onValueChange = {},
                enabled = false,
                modifier = Modifier.fillMaxWidth().clickable { showDatePicker = true },
                trailingIcon = { Icon(Icons.Default.CalendarMonth, null) },
                colors = OutlinedTextFieldDefaults.colors(
                    disabledTextColor = MaterialTheme.colorScheme.onSurface,
                    disabledBorderColor = MaterialTheme.colorScheme.outline,
                    disabledTrailingIconColor = MaterialTheme.colorScheme.onSurfaceVariant
                )
            )
            if (showDatePicker) {
                val dpState = rememberDatePickerState(initialSelectedDateMillis = state.selectedDate)
                DatePickerDialog(
                    onDismissRequest = { showDatePicker = false },
                    confirmButton = { TextButton(onClick = { dpState.selectedDateMillis?.let { viewModel.onDateSelected(it) }; showDatePicker = false }) { Text("OK") } },
                    dismissButton = { TextButton(onClick = { showDatePicker = false }) { Text("Cancel") } }
                ) { DatePicker(state = dpState) }
            }

            // Note
            AddIncomeLabel("Note (optional)")
            OutlinedTextField(
                value = state.note,
                onValueChange = viewModel::onNoteChanged,
                placeholder = { Text("Add a note...") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 2
            )

            Spacer(Modifier.height(24.dp))
        }

        if (state.isLoading) {
            Box(modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.2f)), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = SpendlyGreen)
            }
        }
    }
}

@Composable
private fun AddIncomeLabel(text: String) {
    Text(text, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
}
