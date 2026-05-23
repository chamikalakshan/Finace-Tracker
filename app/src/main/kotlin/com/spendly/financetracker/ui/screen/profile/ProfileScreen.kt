package com.spendly.financetracker.ui.screen.profile

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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.CurrencyExchange
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.spendly.financetracker.ui.theme.SpendlyGray300
import com.spendly.financetracker.ui.theme.SpendlyGray50
import com.spendly.financetracker.ui.theme.SpendlyGray500
import com.spendly.financetracker.ui.theme.SpendlyGray700
import com.spendly.financetracker.ui.theme.SpendlyGray900
import com.spendly.financetracker.ui.theme.SpendlyGreen
import com.spendly.financetracker.ui.theme.SpendlyGreenLight
import com.spendly.financetracker.ui.theme.SpendlyRed
import com.spendly.financetracker.ui.util.displayNameFromEmail
import com.spendly.financetracker.ui.util.formatMoney
import com.spendly.financetracker.ui.util.initialsFromEmail
import com.spendly.financetracker.ui.viewmodel.FinanceUiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(state: FinanceUiState, onSignOut: () -> Unit) {
    val email = state.session?.email.orEmpty()
    val name = state.profile?.name?.takeIf { it.isNotBlank() } ?: displayNameFromEmail(email)
    val initials = initialsFromEmail(name)

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Profile",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Box(
                    modifier = Modifier
                        .size(100.dp)
                        .clip(CircleShape)
                        .background(SpendlyGreenLight),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = initials,
                        style = MaterialTheme.typography.displayMedium,
                        color = SpendlyGreen,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(Modifier.size(16.dp))
                Text(
                    text = name,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = email.ifBlank { "No email available" },
                    style = MaterialTheme.typography.bodyMedium,
                    color = SpendlyGray500
                )
            }

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                ProfileSectionLabel("Preferences")
                ProfileItem(
                    icon = Icons.Default.CurrencyExchange,
                    label = "Default Currency",
                    value = state.profile?.defaultCurrency ?: "LKR"
                )
                ProfileItem(
                    icon = Icons.Default.Settings,
                    label = "Net Savings",
                    value = formatMoney(state.balanceCents)
                )
            }

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                ProfileSectionLabel("Account")
                ProfileItem(
                    icon = Icons.Default.Logout,
                    label = "Logout",
                    labelColor = SpendlyRed,
                    showChevron = false,
                    onClick = onSignOut
                )
            }
        }
    }
}

@Composable
private fun ProfileSectionLabel(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelSmall,
        color = SpendlyGray500,
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 8.dp, bottom = 4.dp)
    )
}

@Composable
private fun ProfileItem(
    icon: ImageVector,
    label: String,
    value: String? = null,
    labelColor: Color = SpendlyGray900,
    showChevron: Boolean = true,
    onClick: () -> Unit = {}
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(12.dp),
        color = SpendlyGray50,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                icon,
                null,
                tint = if (labelColor == SpendlyRed) SpendlyRed else SpendlyGray700,
                modifier = Modifier.size(20.dp)
            )
            Spacer(Modifier.width(16.dp))
            Text(
                label,
                style = MaterialTheme.typography.bodyLarge,
                color = labelColor,
                modifier = Modifier.weight(1f)
            )
            if (value != null) {
                Text(value, style = MaterialTheme.typography.bodyMedium, color = SpendlyGray500)
            }
            if (showChevron) {
                Icon(Icons.Default.ChevronRight, null, tint = SpendlyGray300, modifier = Modifier.size(20.dp))
            }
        }
    }
}
