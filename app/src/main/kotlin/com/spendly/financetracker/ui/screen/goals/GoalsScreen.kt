package com.spendly.financetracker.ui.screen.goals

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Laptop
import androidx.compose.material.icons.filled.TrackChanges
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.spendly.financetracker.ui.components.NoGoalState
import com.spendly.financetracker.ui.theme.SpendlyGray300
import com.spendly.financetracker.ui.theme.SpendlyGray500
import com.spendly.financetracker.ui.theme.SpendlyGray700
import com.spendly.financetracker.ui.theme.SpendlyGray900
import com.spendly.financetracker.ui.theme.SpendlyGreen
import com.spendly.financetracker.ui.theme.SpendlyGreenDark
import com.spendly.financetracker.ui.theme.SpendlyGreenLight
import com.spendly.financetracker.ui.util.formatMoney
import com.spendly.financetracker.ui.viewmodel.FinanceUiState
import com.spendly.financetracker.ui.viewmodel.Goal
import com.spendly.financetracker.ui.viewmodel.GoalDraft
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

typealias OnAddGoal = () -> Unit
typealias OnGoalSelected = (String) -> Unit
typealias OnSaveGoal = (GoalDraft) -> Boolean
typealias OnUpdateGoal = (String, GoalDraft) -> Boolean
typealias OnDeleteGoal = (String) -> Boolean
typealias OnAddSavings = (String, String) -> Boolean

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun GoalsScreenContent(
    state: FinanceUiState,
    onAddGoal: OnAddGoal,
    onGoalSelected: OnGoalSelected
) {
    val otherGoals = state.goals.filter { !it.isPrimary }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Goal Tracker", fontWeight = FontWeight.Bold) })
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAddGoal,
                modifier = Modifier
                    .size(56.dp)
                    .offset(y = 8.dp),
                shape = CircleShape,
                containerColor = SpendlyGreen,
                contentColor = Color.White
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add new goal")
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(start = 16.dp, top = 16.dp, end = 16.dp, bottom = 104.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            if (state.goals.isEmpty()) {
                item {
                    NoGoalState(onSetGoal = onAddGoal)
                }
            } else {
                state.primaryGoal?.let { goal ->
                    item {
                        Text(
                            "Primary Goal",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    item {
                        PrimaryGoalCard(
                            goal = goal,
                            onClick = { onGoalSelected(goal.id) }
                        )
                    }
                }

                if (otherGoals.isNotEmpty()) {
                    item {
                        Text(
                            "Other Goals",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    items(otherGoals, key = { it.id }) { goal ->
                        OtherGoalRow(
                            goal = goal,
                            onClick = { onGoalSelected(goal.id) }
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun EditGoalScreenContent(
    onBack: () -> Unit,
    goal: Goal,
    onSave: OnUpdateGoal,
    onDelete: OnDeleteGoal
) {
    var goalName by rememberSaveable { mutableStateOf(goal.title) }
    var status by rememberSaveable { mutableStateOf(goal.status) }
    var targetAmount by rememberSaveable { mutableStateOf((goal.targetCents / 100L).toString()) }
    var targetDate by rememberSaveable { mutableStateOf(goal.dueDate) }
    var showDatePicker by rememberSaveable { mutableStateOf(false) }
    val datePickerState = rememberDatePickerState()

    fun saveGoal() {
        onSave(goal.id, GoalDraft(
            title = goalName,
            status = status,
            targetAmount = targetAmount,
            targetDate = targetDate,
            initialSaved = ""
        ))
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                title = { Text("Edit Goal", fontWeight = FontWeight.Bold) }
            )
        },
        bottomBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Button(
                    onClick = { onDelete(goal.id) },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = Color(0xFFEF5350)),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(44.dp),
                    border = BorderStroke(1.dp, Color(0xFFEF5350)),
                    contentPadding = PaddingValues(horizontal = 22.dp, vertical = 0.dp)
                ) {
                    Icon(Icons.Default.Delete, contentDescription = null, tint = Color(0xFFEF5350))
                    Spacer(Modifier.width(8.dp))
                    Text("Delete", style = MaterialTheme.typography.labelMedium)
                }

                Button(
                    onClick = ::saveGoal,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = SpendlyGreen,
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(44.dp),
                    contentPadding = PaddingValues(horizontal = 22.dp, vertical = 0.dp)
                ) {
                    Text("Save", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(start = 24.dp, top = 20.dp, end = 24.dp, bottom = 104.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .background(SpendlyGreenLight, RoundedCornerShape(12.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.TrackChanges,
                        contentDescription = null,
                        tint = SpendlyGreen
                    )
                }
                Spacer(Modifier.width(12.dp))
                Column {
                    Text(
                        "Edit Goal",
                        style = MaterialTheme.typography.titleMedium,
                        color = SpendlyGray900,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        "Update your goal details",
                        style = MaterialTheme.typography.bodySmall,
                        color = SpendlyGray500
                    )
                }
            }

            GoalFormField(
                label = "Goal Name",
                value = goalName,
                onValueChange = { goalName = it },
                placeholder = "e.g. Dream Vacation"
            )
            GoalStatusSelector(
                selectedStatus = status,
                onStatusSelected = { status = it }
            )
            GoalFormField(
                label = "Target Amount (LKR)",
                value = targetAmount,
                onValueChange = { targetAmount = it },
                placeholder = "0",
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
            )
            GoalDateField(
                label = "Target Date",
                value = targetDate,
                placeholder = "Select a date",
                onClick = { showDatePicker = true }
            )

            // Current savings card
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = SpendlyGreenLight),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Current Savings (auto-calculated)", style = MaterialTheme.typography.labelSmall, color = SpendlyGray700)
                    Text(formatMoney(goal.savedCents), style = MaterialTheme.typography.titleLarge, color = SpendlyGreen, fontWeight = FontWeight.Bold)
                    Text("${goal.progressPercent}% of ${formatMoney(goal.targetCents)}", style = MaterialTheme.typography.bodySmall, color = SpendlyGray700)
                    Spacer(Modifier.height(8.dp))
                    LinearProgressIndicator(
                        progress = { goal.progressPercent / 100f },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp)),
                        color = SpendlyGreen,
                        trackColor = SpendlyGreenLight
                    )
                }
            }

            // spacer where bottom bar provides actions
        }
    }

    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let { millis ->
                            targetDate = formatGoalDate(millis)
                        }
                        showDatePicker = false
                    }
                ) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("Cancel")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun AddGoalScreenContent(
    onBack: () -> Unit,
    onSave: OnSaveGoal
) {
    var goalName by rememberSaveable { mutableStateOf("") }
    var status by rememberSaveable { mutableStateOf("On track") }
    var targetAmount by rememberSaveable { mutableStateOf("") }
    var targetDate by rememberSaveable { mutableStateOf("") }
    var initialSaved by rememberSaveable { mutableStateOf("") }
    var showDatePicker by rememberSaveable { mutableStateOf(false) }
    val datePickerState = rememberDatePickerState()

    fun saveGoal() {
        onSave(
            GoalDraft(
                title = goalName,
                status = status,
                targetAmount = targetAmount,
                targetDate = targetDate,
                initialSaved = initialSaved
            )
        )
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                title = { Text("Add New Goal", fontWeight = FontWeight.Bold) }
            )
        },
        floatingActionButton = {
            Button(
                onClick = ::saveGoal,
                colors = ButtonDefaults.buttonColors(
                    containerColor = SpendlyGreen,
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.height(44.dp),
                contentPadding = PaddingValues(horizontal = 22.dp, vertical = 0.dp)
            ) {
                Text("Save", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(start = 24.dp, top = 20.dp, end = 24.dp, bottom = 104.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .background(SpendlyGreenLight, RoundedCornerShape(12.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.TrackChanges,
                        contentDescription = null,
                        tint = SpendlyGreen
                    )
                }
                Spacer(Modifier.width(12.dp))
                Column {
                    Text(
                        "Set a Target",
                        style = MaterialTheme.typography.titleMedium,
                        color = SpendlyGray900,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        "Plan your next big purchase",
                        style = MaterialTheme.typography.bodySmall,
                        color = SpendlyGray500
                    )
                }
            }

            GoalFormField(
                label = "Goal Name",
                value = goalName,
                onValueChange = { goalName = it },
                placeholder = "e.g. Dream Vacation"
            )
            GoalStatusSelector(
                selectedStatus = status,
                onStatusSelected = { status = it }
            )
            GoalFormField(
                label = "Target Amount",
                value = targetAmount,
                onValueChange = { targetAmount = it },
                placeholder = "0",
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
            )
            GoalDateField(
                label = "Target Date",
                value = targetDate,
                placeholder = "Select a date",
                onClick = { showDatePicker = true }
            )
            GoalFormField(
                label = "Initial Saved (Optional)",
                value = initialSaved,
                onValueChange = { initialSaved = it },
                placeholder = "0",
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
            )
        }
    }

    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let { millis ->
                            targetDate = formatGoalDate(millis)
                        }
                        showDatePicker = false
                    }
                ) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("Cancel")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
}

@Composable
private fun GoalStatusSelector(
    selectedStatus: String,
    onStatusSelected: (String) -> Unit
) {
    val statuses = listOf("On track", "Not On track")

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = "Status",
            style = MaterialTheme.typography.labelSmall,
            color = SpendlyGray900,
            fontWeight = FontWeight.Bold
        )
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            statuses.forEach { status ->
                FilterChip(
                    selected = selectedStatus == status,
                    onClick = { onStatusSelected(status) },
                    label = { Text(status) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = SpendlyGreen,
                        selectedLabelColor = Color.White
                    )
                )
            }
        }
    }
}

@Composable
private fun GoalDateField(
    label: String,
    value: String,
    placeholder: String,
    onClick: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = SpendlyGray900,
            fontWeight = FontWeight.Bold
        )
        Box {
            OutlinedTextField(
                value = value,
                onValueChange = {},
                placeholder = { Text(placeholder, color = SpendlyGray500) },
                trailingIcon = {
                    Icon(Icons.Default.CalendarMonth, contentDescription = null, tint = SpendlyGray700)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                singleLine = true,
                readOnly = true,
                shape = RoundedCornerShape(12.dp)
            )
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .clickable(onClick = onClick)
            )
        }
    }
}

@Composable
private fun GoalFormField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = SpendlyGray900,
            fontWeight = FontWeight.Bold
        )
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            placeholder = { Text(placeholder, color = SpendlyGray500) },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            singleLine = true,
            shape = RoundedCornerShape(12.dp),
            keyboardOptions = keyboardOptions
        )
    }
}

private fun formatGoalDate(timeMillis: Long): String =
    SimpleDateFormat("MMM d, yyyy", Locale.getDefault()).format(Date(timeMillis))

private fun requiredMonthlySavingsCents(goal: Goal): Long {
    if (goal.remainingCents <= 0L) return 0L

    val monthsRemaining = monthsUntilDueDate(goal.dueDate)
    return (goal.remainingCents + monthsRemaining - 1L) / monthsRemaining
}

private fun monthsUntilDueDate(dueDate: String): Long {
    val due = parseGoalDueDate(dueDate) ?: return 12L
    val now = Calendar.getInstance()
    val target = Calendar.getInstance().apply { time = due }
    val monthDelta = (target.get(Calendar.YEAR) - now.get(Calendar.YEAR)) * 12 +
        (target.get(Calendar.MONTH) - now.get(Calendar.MONTH))
    val dayAdjustment = if (target.get(Calendar.DAY_OF_MONTH) > now.get(Calendar.DAY_OF_MONTH)) 1 else 0

    return (monthDelta + dayAdjustment).coerceAtLeast(1).toLong()
}

private fun parseGoalDueDate(dueDate: String): Date? {
    val patterns = listOf("MMM d, yyyy", "MMM yyyy", "MMMM yyyy")

    return patterns.firstNotNullOfOrNull { pattern ->
        runCatching {
            SimpleDateFormat(pattern, Locale.getDefault()).apply {
                isLenient = false
            }.parse(dueDate)
        }.getOrNull()
    }
}

@Composable
private fun PrimaryGoalCard(
    goal: Goal,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = SpendlyGreen),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    goal.title,
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.headlineSmall,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
                Surface(
                    color = SpendlyGreenDark,
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(Icons.Default.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(12.dp))
                        Text(goal.status.uppercase(), color = Color.White, fontWeight = FontWeight.Bold, fontSize = 10.sp)
                    }
                }
            }

            Text(
                "${goal.dueDate} - ${formatMoney(goal.remainingCents)} remaining",
                style = MaterialTheme.typography.bodySmall,
                color = Color.White.copy(alpha = 0.82f)
            )

            LinearProgressIndicator(
                progress = { goal.progressPercent / 100f },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp)),
                color = Color.White,
                trackColor = Color.White.copy(alpha = 0.3f)
            )

            Row {
                Text(
                    formatMoney(goal.savedCents),
                    style = MaterialTheme.typography.labelMedium,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    " / ${formatMoney(goal.targetCents)}",
                    style = MaterialTheme.typography.labelMedium,
                    color = Color.White.copy(alpha = 0.8f)
                )
            }
        }
    }
}

@Composable
private fun OtherGoalRow(
    goal: Goal,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(0.5.dp, SpendlyGray300),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(SpendlyGreenLight, RoundedCornerShape(10.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    if (goal.title.contains("mac", ignoreCase = true)) Icons.Default.Laptop else Icons.Default.Flag,
                    contentDescription = null,
                    tint = SpendlyGreen
                )
            }
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Row {
                    Text(goal.title, modifier = Modifier.weight(1f), color = SpendlyGray900, fontWeight = FontWeight.Bold)
                    Text("${goal.progressPercent}%", color = SpendlyGreen, fontWeight = FontWeight.Bold)
                }
                LinearProgressIndicator(
                    progress = { goal.progressPercent / 100f },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp)),
                    color = SpendlyGreen,
                    trackColor = SpendlyGreenLight
                )
                Text(
                    "${formatMoney(goal.savedCents)} / ${formatMoney(goal.targetCents)}",
                    style = MaterialTheme.typography.labelSmall,
                    color = SpendlyGray500
                )
            }
            Icon(Icons.Default.ChevronRight, contentDescription = null, tint = SpendlyGray700)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun GoalDetailScreenContent(
    state: FinanceUiState,
    goalId: String?,
    onAddSavings: OnAddSavings,
    onEdit: (String) -> Unit,
    onBack: () -> Unit
) {
    val goal = goalId?.let { selectedGoalId ->
        state.goals.firstOrNull { it.id == selectedGoalId }
    }
    var showAddSavingsDialog by rememberSaveable(goal?.id) { mutableStateOf(false) }
    var savingsAmount by rememberSaveable(goal?.id) { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                title = { Text(goal?.title ?: "Goal Details", fontWeight = FontWeight.Bold) },
                actions = {
                    if (goal != null) {
                        IconButton(onClick = { onEdit(goal.id) }) {
                            Icon(Icons.Default.Edit, contentDescription = "Edit Goal")
                        }
                    }
                }
            )
        }
    ) { padding ->
        if (goal == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                NoGoalState(onSetGoal = onBack)
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
                    GoalDetailSummaryCard(
                        goal = goal,
                        onAddSavings = { showAddSavingsDialog = true }
                    )
                }
                item {
                    GoalMonthlySavingsCard(goal = goal)
                }
            }
        }
    }

    if (goal != null && showAddSavingsDialog) {
        AddSavingsDialog(
            goalTitle = goal.title,
            amount = savingsAmount,
            onAmountChange = { savingsAmount = it },
            onDismiss = { showAddSavingsDialog = false },
            onApply = {
                if (onAddSavings(goal.id, savingsAmount)) {
                    savingsAmount = ""
                    showAddSavingsDialog = false
                }
            }
        )
    }
}

@Composable
private fun GoalDetailSummaryCard(
    goal: Goal,
    onAddSavings: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = SpendlyGreen),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(verticalAlignment = Alignment.Top) {
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .background(Color.White.copy(alpha = 0.18f), RoundedCornerShape(10.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            if (goal.title.contains("mac", ignoreCase = true)) Icons.Default.Laptop else Icons.Default.Flag,
                            contentDescription = null,
                            tint = Color.White
                        )
                    }
                    Text(
                        goal.title,
                        style = MaterialTheme.typography.headlineSmall,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text(
                            "Target: ${goal.dueDate}",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White.copy(alpha = 0.82f)
                        )
                        Text(
                            "Remaining: ${formatMoney(goal.remainingCents)}",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White.copy(alpha = 0.82f)
                        )
                    }
                }

                Column(
                    horizontalAlignment = Alignment.End,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Surface(
                        color = SpendlyGreenDark,
                        shape = RoundedCornerShape(20.dp),
                        border = BorderStroke(0.5.dp, Color.White.copy(alpha = 0.65f))
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(Icons.Default.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(12.dp))
                            Text(goal.status.uppercase(), color = Color.White, fontWeight = FontWeight.Bold, fontSize = 10.sp)
                        }
                    }
                    Button(
                        onClick = onAddSavings,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = SpendlyGreenDark,
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(20.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp),
                        modifier = Modifier.height(34.dp)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("Add Savings", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            LinearProgressIndicator(
                progress = { goal.progressPercent / 100f },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(9.dp)
                    .clip(RoundedCornerShape(5.dp)),
                color = Color.White,
                trackColor = Color.White.copy(alpha = 0.3f)
            )

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    "${formatMoney(goal.savedCents)} (${goal.progressPercent}%)",
                    style = MaterialTheme.typography.labelMedium,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    formatMoney(goal.targetCents),
                    style = MaterialTheme.typography.labelMedium,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    "Saved: ${formatMoney(goal.savedCents)}",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.White.copy(alpha = 0.9f),
                    modifier = Modifier.weight(1f)
                )
                Text(
                    "Saving / month: ${formatMoney(requiredMonthlySavingsCents(goal))}",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.White.copy(alpha = 0.9f)
                )
            }
        }
    }
}

@Composable
private fun GoalMonthlySavingsCard(goal: Goal) {
    val savings = monthlySavingsData(goal)
    val maxAmount = savings.maxOfOrNull { it.amountCents }?.coerceAtLeast(1L) ?: 1L

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(0.5.dp, SpendlyGray300),
        shape = RoundedCornerShape(24.dp)
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 24.dp, vertical = 22.dp),
            verticalArrangement = Arrangement.spacedBy(22.dp)
        ) {
            Text(
                "Monthly Savings",
                style = MaterialTheme.typography.titleLarge,
                color = SpendlyGray900,
                fontWeight = FontWeight.Bold
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(170.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.Bottom
            ) {
                savings.forEach { item ->
                    GoalMonthlySavingsBar(
                        item = item,
                        maxAmount = maxAmount,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

@Composable
private fun GoalMonthlySavingsBar(
    item: GoalMonthlySaving,
    maxAmount: Long,
    modifier: Modifier = Modifier
) {
    val percent = (item.amountCents.toFloat() / maxAmount.toFloat()).coerceIn(0f, 1f)
    val barHeight = if (item.amountCents <= 0L) 0.dp else 14.dp + (112.dp * percent)

    Column(
        modifier = modifier.height(170.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Bottom
    ) {
        Column(
            modifier = Modifier.height(138.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Bottom
        ) {
            Text(
                formatCompactChartAmount(item.amountCents),
                style = MaterialTheme.typography.labelSmall,
                color = SpendlyGray500,
                textAlign = TextAlign.Center,
                maxLines = 1
            )
            Spacer(Modifier.height(5.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.72f)
                    .height(barHeight)
                    .clip(RoundedCornerShape(6.dp))
                    .background(SpendlyGreen)
            )
        }
        Spacer(Modifier.height(8.dp))
        Text(
            item.month,
            style = MaterialTheme.typography.bodySmall,
            color = SpendlyGray500,
            textAlign = TextAlign.Center,
            maxLines = 1
        )
    }
}

@Composable
private fun AddSavingsDialog(
    goalTitle: String,
    amount: String,
    onAmountChange: (String) -> Unit,
    onDismiss: () -> Unit,
    onApply: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Savings") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    "Add the amount you saved for $goalTitle.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = SpendlyGray700
                )
                OutlinedTextField(
                    value = amount,
                    onValueChange = onAmountChange,
                    placeholder = { Text("0") },
                    label = { Text("Saved amount") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                )
            }
        },
        confirmButton = {
            Button(
                onClick = onApply,
                colors = ButtonDefaults.buttonColors(
                    containerColor = SpendlyGreen,
                    contentColor = Color.White
                )
            ) {
                Text("Apply")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

private data class GoalMonthlySaving(
    val month: String,
    val amountCents: Long
)

private fun monthlySavingsData(goal: Goal): List<GoalMonthlySaving> {
    val monthLabels = lastFiveGoalMonthLabels()
    return monthLabels.mapIndexed { index, label ->
        GoalMonthlySaving(
            month = label,
            amountCents = if (index == monthLabels.lastIndex) goal.savedCents else 0L
        )
    }
}

private fun lastFiveGoalMonthLabels(): List<String> {
    val formatter = SimpleDateFormat("MMM", Locale.getDefault())
    val calendar = Calendar.getInstance()

    return (4 downTo 0).map { offset ->
        val monthCalendar = calendar.clone() as Calendar
        monthCalendar.add(Calendar.MONTH, -offset)
        formatter.format(Date(monthCalendar.timeInMillis))
    }
}

private fun formatCompactChartAmount(cents: Long): String {
    val amount = cents / 100L

    return when {
        amount >= 1_000_000L -> "${(amount + 500_000L) / 1_000_000L}M"
        amount >= 1_000L -> "${(amount + 500L) / 1_000L}K"
        else -> amount.toString()
    }
}
