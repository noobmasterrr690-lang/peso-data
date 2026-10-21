package com.aistudio.peso.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aistudio.peso.data.model.Budget
import com.aistudio.peso.data.model.Expense
import com.aistudio.peso.data.model.Income
import com.aistudio.peso.data.model.PesoConstants
import com.aistudio.peso.data.model.PesoDateUtils
import com.aistudio.peso.data.model.QuickPreset
import com.aistudio.peso.data.model.SavingsGoal
import com.aistudio.peso.data.model.UserProfile
import com.aistudio.peso.ui.theme.PesoVoltGreen

@Composable
fun EditBalanceDialog(
    currentBalance: Double,
    onDismiss: () -> Unit,
    onConfirm: (Double) -> Unit
) {
    var amountText by remember { mutableStateOf(if (currentBalance > 0) currentBalance.toInt().toString() else "") }
    var error by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Update Current Balance", fontWeight = FontWeight.Bold) },
        text = {
            Column {
                Text(
                    "Set how much cash or wallet balance you currently have in Philippine Pesos (₱).",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedTextField(
                    value = amountText,
                    onValueChange = {
                        amountText = it.filter { char -> char.isDigit() || char == '.' }
                        error = null
                    },
                    label = { Text("Balance (₱)") },
                    prefix = { Text("₱ ", fontWeight = FontWeight.Bold) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    isError = error != null,
                    supportingText = error?.let { { Text(it) } },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_balance")
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val amount = amountText.toDoubleOrNull()
                    if (amount == null || amount < 0) {
                        error = "Please enter a valid amount"
                    } else {
                        onConfirm(amount)
                    }
                },
                modifier = Modifier.testTag("btn_save_balance")
            ) {
                Text("Save Balance")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AddOrEditExpenseDialog(
    initialExpense: Expense? = null,
    onDismiss: () -> Unit,
    onConfirm: (amount: Double, category: String, date: String, note: String?) -> Unit
) {
    var amountText by remember { mutableStateOf(initialExpense?.amount?.let { if (it % 1 == 0.0) it.toInt().toString() else it.toString() } ?: "") }
    var selectedCategory by remember { mutableStateOf(initialExpense?.category ?: "Food") }
    var dateText by remember { mutableStateOf(initialExpense?.date ?: PesoDateUtils.todayKey()) }
    var noteText by remember { mutableStateOf(initialExpense?.note ?: "") }
    var error by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (initialExpense == null) "Log Expense" else "Edit Expense", fontWeight = FontWeight.Bold) },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                OutlinedTextField(
                    value = amountText,
                    onValueChange = {
                        amountText = it.filter { char -> char.isDigit() || char == '.' }
                        error = null
                    },
                    label = { Text("Amount (₱)") },
                    prefix = { Text("₱ ", fontWeight = FontWeight.Bold) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    isError = error != null,
                    supportingText = error?.let { { Text(it) } },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_expense_amount")
                )

                Text("Category", style = MaterialTheme.typography.titleMedium)
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    PesoConstants.CATEGORIES.forEach { cat ->
                        FilterChip(
                            selected = selectedCategory == cat.id,
                            onClick = { selectedCategory = cat.id },
                            label = { Text("${cat.icon} ${cat.id}") },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                            ),
                            modifier = Modifier.testTag("chip_cat_${cat.id}")
                        )
                    }
                }

                OutlinedTextField(
                    value = noteText,
                    onValueChange = { noteText = it },
                    label = { Text("Note / Description (Optional)") },
                    placeholder = { Text("e.g. Lunch with classmates, jeepney fare") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_expense_note")
                )

                OutlinedTextField(
                    value = dateText,
                    onValueChange = { dateText = it },
                    label = { Text("Date (YYYY-MM-DD)") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_expense_date")
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val amount = amountText.toDoubleOrNull()
                    if (amount == null || amount <= 0) {
                        error = "Please enter an amount > ₱0"
                    } else {
                        onConfirm(amount, selectedCategory, dateText, noteText.trim().ifEmpty { null })
                    }
                },
                modifier = Modifier.testTag("btn_save_expense")
            ) {
                Text(if (initialExpense == null) "Log Expense" else "Save Changes")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AddOrEditIncomeDialog(
    initialIncome: Income? = null,
    onDismiss: () -> Unit,
    onConfirm: (amount: Double, source: String, date: String, note: String?) -> Unit
) {
    var amountText by remember { mutableStateOf(initialIncome?.amount?.let { if (it % 1 == 0.0) it.toInt().toString() else it.toString() } ?: "") }
    var selectedSource by remember { mutableStateOf(initialIncome?.source ?: "Allowance") }
    var dateText by remember { mutableStateOf(initialIncome?.date ?: PesoDateUtils.todayKey()) }
    var noteText by remember { mutableStateOf(initialIncome?.note ?: "") }
    var error by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (initialIncome == null) "Add Income" else "Edit Income", fontWeight = FontWeight.Bold) },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                OutlinedTextField(
                    value = amountText,
                    onValueChange = {
                        amountText = it.filter { char -> char.isDigit() || char == '.' }
                        error = null
                    },
                    label = { Text("Amount (₱)") },
                    prefix = { Text("₱ ", fontWeight = FontWeight.Bold) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    isError = error != null,
                    supportingText = error?.let { { Text(it) } },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_income_amount")
                )

                Text("Source", style = MaterialTheme.typography.titleMedium)
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    PesoConstants.INCOME_SOURCES.forEach { (src, icon) ->
                        FilterChip(
                            selected = selectedSource == src,
                            onClick = { selectedSource = src },
                            label = { Text("$icon $src") },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                            ),
                            modifier = Modifier.testTag("chip_src_$src")
                        )
                    }
                }

                OutlinedTextField(
                    value = noteText,
                    onValueChange = { noteText = it },
                    label = { Text("Note / Description (Optional)") },
                    placeholder = { Text("e.g. Allowance from nanay, scholarship stipend") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_income_note")
                )

                OutlinedTextField(
                    value = dateText,
                    onValueChange = { dateText = it },
                    label = { Text("Date (YYYY-MM-DD)") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_income_date")
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val amount = amountText.toDoubleOrNull()
                    if (amount == null || amount <= 0) {
                        error = "Please enter an amount > ₱0"
                    } else {
                        onConfirm(amount, selectedSource, dateText, noteText.trim().ifEmpty { null })
                    }
                },
                modifier = Modifier.testTag("btn_save_income")
            ) {
                Text(if (initialIncome == null) "Save Income" else "Save Changes")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun EditBudgetDialog(
    currentBudget: Budget,
    onDismiss: () -> Unit,
    onConfirm: (totalLimit: Double, categoryLimits: Map<String, Double>) -> Unit
) {
    var totalText by remember { mutableStateOf(if (currentBudget.totalLimit > 0) currentBudget.totalLimit.toInt().toString() else "") }
    val catLimitsState = remember {
        mutableStateMapOf<String, String>().apply {
            PesoConstants.CATEGORIES.forEach { cat ->
                val existing = currentBudget.categoryLimits[cat.id]
                put(cat.id, existing?.let { if (it > 0) it.toInt().toString() else "" } ?: "")
            }
        }
    }
    var error by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Monthly Spending Budget", fontWeight = FontWeight.Bold) },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    "Set the maximum amount you want to spend this month. PESO will calculate your safe daily allowance and warn you before exceeding limits.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                OutlinedTextField(
                    value = totalText,
                    onValueChange = {
                        totalText = it.filter { char -> char.isDigit() || char == '.' }
                        error = null
                    },
                    label = { Text("Total Monthly Limit (₱)") },
                    prefix = { Text("₱ ", fontWeight = FontWeight.Bold) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    isError = error != null,
                    supportingText = error?.let { { Text(it) } },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_budget_total")
                )

                HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                Text("Category Limits (Optional)", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)

                PesoConstants.CATEGORIES.forEach { cat ->
                    OutlinedTextField(
                        value = catLimitsState[cat.id] ?: "",
                        onValueChange = { newVal ->
                            catLimitsState[cat.id] = newVal.filter { it.isDigit() || it == '.' }
                        },
                        label = { Text("${cat.icon} ${cat.id} Limit (₱)") },
                        prefix = { Text("₱ ") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_cat_limit_${cat.id}")
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val total = totalText.toDoubleOrNull()
                    if (total == null || total <= 0) {
                        error = "Please enter a valid monthly limit"
                    } else {
                        val catMap = mutableMapOf<String, Double>()
                        for ((k, v) in catLimitsState) {
                            val limit = v.toDoubleOrNull()
                            if (limit != null && limit > 0) {
                                catMap[k] = limit
                            }
                        }
                        onConfirm(total, catMap)
                    }
                },
                modifier = Modifier.testTag("btn_save_budget")
            ) {
                Text("Save Budget")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun AddGoalDialog(
    onDismiss: () -> Unit,
    onConfirm: (name: String, targetAmount: Double, initialDeposit: Double, targetDate: String?) -> Unit
) {
    var nameText by remember { mutableStateOf("") }
    var targetText by remember { mutableStateOf("") }
    var initialDepositText by remember { mutableStateOf("") }
    var targetDateText by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("New Savings Goal", fontWeight = FontWeight.Bold) },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                OutlinedTextField(
                    value = nameText,
                    onValueChange = { nameText = it },
                    label = { Text("Goal Name") },
                    placeholder = { Text("e.g. New Laptop, Emergency Fund") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_goal_name")
                )

                OutlinedTextField(
                    value = targetText,
                    onValueChange = {
                        targetText = it.filter { char -> char.isDigit() || char == '.' }
                        error = null
                    },
                    label = { Text("Target Amount (₱)") },
                    prefix = { Text("₱ ", fontWeight = FontWeight.Bold) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    isError = error != null,
                    supportingText = error?.let { { Text(it) } },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_goal_target")
                )

                OutlinedTextField(
                    value = initialDepositText,
                    onValueChange = { initialDepositText = it.filter { char -> char.isDigit() || char == '.' } },
                    label = { Text("Initial Deposit (Optional)") },
                    prefix = { Text("₱ ") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    placeholder = { Text("0") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_goal_initial")
                )

                OutlinedTextField(
                    value = targetDateText,
                    onValueChange = { targetDateText = it },
                    label = { Text("Target Date (YYYY-MM-DD, Optional)") },
                    placeholder = { Text("2026-12-31") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_goal_date")
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val target = targetText.toDoubleOrNull()
                    if (nameText.isBlank()) {
                        error = "Please enter a name for your goal"
                    } else if (target == null || target <= 0) {
                        error = "Target amount must be > ₱0"
                    } else {
                        val initDeposit = initialDepositText.toDoubleOrNull() ?: 0.0
                        onConfirm(nameText.trim(), target, initDeposit, targetDateText.trim().ifEmpty { null })
                    }
                },
                modifier = Modifier.testTag("btn_save_goal")
            ) {
                Text("Create Goal")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun DepositWithdrawDialog(
    isDeposit: Boolean,
    goal: SavingsGoal,
    walletBalance: Double,
    onDismiss: () -> Unit,
    onConfirm: (Double) -> Unit
) {
    var amountText by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (isDeposit) "Deposit to ${goal.name}" else "Withdraw from ${goal.name}", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    if (isDeposit)
                        "Money will be moved from your wallet balance (${PesoDateUtils.formatCurrency(walletBalance)}) to this goal."
                    else
                        "Money will be moved from this savings goal (${PesoDateUtils.formatCurrency(goal.currentAmount)}) back into your active wallet.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                OutlinedTextField(
                    value = amountText,
                    onValueChange = {
                        amountText = it.filter { char -> char.isDigit() || char == '.' }
                        error = null
                    },
                    label = { Text("Amount (₱)") },
                    prefix = { Text("₱ ", fontWeight = FontWeight.Bold) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    isError = error != null,
                    supportingText = error?.let { { Text(it) } },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_goal_amount")
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val amount = amountText.toDoubleOrNull()
                    if (amount == null || amount <= 0) {
                        error = "Enter a valid amount"
                    } else if (isDeposit && amount > walletBalance) {
                        error = "Amount exceeds wallet balance"
                    } else if (!isDeposit && amount > goal.currentAmount) {
                        error = "Amount exceeds saved goal amount"
                    } else {
                        onConfirm(amount)
                    }
                },
                modifier = Modifier.testTag("btn_confirm_goal_transfer")
            ) {
                Text(if (isDeposit) "Deposit" else "Withdraw")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ManagePresetsDialog(
    presets: List<QuickPreset>,
    onDismiss: () -> Unit,
    onAddPreset: (label: String, amount: Double, classification: String, note: String?) -> Unit,
    onDeletePreset: (QuickPreset) -> Unit,
    onResetPresets: () -> Unit
) {
    var isAdding by remember { mutableStateOf(false) }
    var labelText by remember { mutableStateOf("") }
    var amountText by remember { mutableStateOf("") }
    var selectedClass by remember { mutableStateOf("Fare") }
    var noteText by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("1-Tap Spend Shortcuts", fontWeight = FontWeight.Bold)
                if (!isAdding) {
                    IconButton(onClick = { isAdding = true }, modifier = Modifier.testTag("btn_open_add_shortcut")) {
                        Icon(Icons.Default.Add, contentDescription = "Add Shortcut")
                    }
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (isAdding) {
                    Text("New 1-Tap Shortcut", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                    OutlinedTextField(
                        value = labelText,
                        onValueChange = { labelText = it },
                        label = { Text("Shortcut Name") },
                        placeholder = { Text("e.g. MRT Fare, Turon Snack") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_preset_label")
                    )

                    OutlinedTextField(
                        value = amountText,
                        onValueChange = {
                            amountText = it.filter { char -> char.isDigit() || char == '.' }
                            error = null
                        },
                        label = { Text("Amount (₱)") },
                        prefix = { Text("₱ ", fontWeight = FontWeight.Bold) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        isError = error != null,
                        supportingText = error?.let { { Text(it) } },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_preset_amount")
                    )

                    Text("Classification", style = MaterialTheme.typography.labelLarge)
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        PesoConstants.QUICK_CLASSIFICATIONS.forEach { c ->
                            FilterChip(
                                selected = selectedClass == c.id,
                                onClick = { selectedClass = c.id },
                                label = { Text(c.label) }
                            )
                        }
                    }

                    OutlinedTextField(
                        value = noteText,
                        onValueChange = { noteText = it },
                        label = { Text("Note (Optional)") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(onClick = { isAdding = false }) {
                            Text("Cancel")
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = {
                                val amount = amountText.toDoubleOrNull()
                                if (labelText.isBlank()) {
                                    error = "Enter a name"
                                } else if (amount == null || amount <= 0) {
                                    error = "Enter amount > ₱0"
                                } else {
                                    onAddPreset(labelText.trim(), amount, selectedClass, noteText.trim().ifEmpty { null })
                                    labelText = ""
                                    amountText = ""
                                    noteText = ""
                                    isAdding = false
                                }
                            },
                            modifier = Modifier.testTag("btn_submit_new_preset")
                        ) {
                            Text("Add Shortcut")
                        }
                    }
                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                }

                Text(
                    "Tap to delete unwanted shortcuts or restore standard Philippine student presets:",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                presets.forEach { preset ->
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(preset.label, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                                Text(
                                    "${preset.classification} • ₱${preset.amount.toInt()}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            IconButton(onClick = { onDeletePreset(preset) }) {
                                Icon(Icons.Default.Delete, contentDescription = "Delete shortcut", tint = MaterialTheme.colorScheme.error)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))
                OutlinedButton(
                    onClick = onResetPresets,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("btn_reset_presets")
                ) {
                    Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Reset to Default Filipino Student Presets")
                }
            }
        },
        confirmButton = {
            Button(onClick = onDismiss) {
                Text("Done")
            }
        }
    )
}

@Composable
fun ProfileDialog(
    userProfile: UserProfile,
    isDarkTheme: Boolean,
    onDismiss: () -> Unit,
    onToggleTheme: () -> Unit,
    onUpdateProfile: (name: String, email: String) -> Unit,
    onResetData: () -> Unit,
    onClearData: () -> Unit
) {
    var nameText by remember { mutableStateOf(userProfile.name) }
    var emailText by remember { mutableStateOf(userProfile.email) }
    var showResetConfirm by remember { mutableStateOf(false) }
    var showClearConfirm by remember { mutableStateOf(false) }

    if (showResetConfirm) {
        AlertDialog(
            onDismissRequest = { showResetConfirm = false },
            title = { Text("Reset to Demo Data?") },
            text = { Text("This will replace current data with initial sample transactions, budget, and savings goals.") },
            confirmButton = {
                Button(onClick = {
                    showResetConfirm = false
                    onResetData()
                    onDismiss()
                }) {
                    Text("Reset")
                }
            },
            dismissButton = {
                TextButton(onClick = { showResetConfirm = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    if (showClearConfirm) {
        AlertDialog(
            onDismissRequest = { showClearConfirm = false },
            title = { Text("Clear All Transactions?") },
            text = { Text("This will erase all expenses, incomes, and savings goals, and reset your balance to ₱0.00.") },
            confirmButton = {
                Button(
                    onClick = {
                        showClearConfirm = false
                        onClearData()
                        onDismiss()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Clear Everything")
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearConfirm = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("User Profile & Settings", fontWeight = FontWeight.Bold) },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                OutlinedTextField(
                    value = nameText,
                    onValueChange = { nameText = it },
                    label = { Text("Your Name / Nickname") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_profile_name")
                )

                OutlinedTextField(
                    value = emailText,
                    onValueChange = { emailText = it },
                    label = { Text("Email / School Handle") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_profile_email")
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Dark Mode", fontWeight = FontWeight.SemiBold)
                        Text(
                            if (isDarkTheme) "Volt Green & Charcoal" else "Emerald & Clean Slate",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Switch(
                        checked = isDarkTheme,
                        onCheckedChange = { onToggleTheme() },
                        modifier = Modifier.testTag("switch_dark_mode")
                    )
                }

                HorizontalDivider()
                Text("Data Management", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)

                OutlinedButton(
                    onClick = { showResetConfirm = true },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("btn_reset_demo")
                ) {
                    Text("Restore Sample Student Data")
                }

                OutlinedButton(
                    onClick = { showClearConfirm = true },
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("btn_clear_data")
                ) {
                    Text("Clear All Data")
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onUpdateProfile(nameText.trim(), emailText.trim())
                    onDismiss()
                },
                modifier = Modifier.testTag("btn_save_profile")
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Close")
            }
        }
    )
}
