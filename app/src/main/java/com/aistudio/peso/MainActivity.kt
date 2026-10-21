package com.aistudio.peso

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingDown
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.TrackChanges
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aistudio.peso.data.model.Expense
import com.aistudio.peso.data.model.Income
import com.aistudio.peso.data.model.PesoDateUtils
import com.aistudio.peso.data.model.SavingsGoal
import com.aistudio.peso.ui.components.AddGoalDialog
import com.aistudio.peso.ui.components.AddOrEditExpenseDialog
import com.aistudio.peso.ui.components.AddOrEditIncomeDialog
import com.aistudio.peso.ui.components.DepositWithdrawDialog
import com.aistudio.peso.ui.components.EditBalanceDialog
import com.aistudio.peso.ui.components.EditBudgetDialog
import com.aistudio.peso.ui.components.ManagePresetsDialog
import com.aistudio.peso.ui.components.ProfileDialog
import com.aistudio.peso.ui.screens.BudgetScreen
import com.aistudio.peso.ui.screens.DashboardScreen
import com.aistudio.peso.ui.screens.ExpensesScreen
import com.aistudio.peso.ui.screens.IncomeScreen
import com.aistudio.peso.ui.screens.ReportsScreen
import com.aistudio.peso.ui.screens.SavingsGoalsScreen
import com.aistudio.peso.ui.theme.PESOTheme
import com.aistudio.peso.ui.viewmodel.PesoViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

enum class ScreenTab(val title: String) {
    HOME("Home"),
    EXPENSES("Expenses"),
    INCOME("Income"),
    BUDGET("Budget"),
    GOALS("Savings"),
    REPORTS("Reports")
}

class MainActivity : ComponentActivity() {

    private val viewModel: PesoViewModel by viewModels()

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val isDarkTheme by viewModel.isDarkTheme.collectAsState()

            PESOTheme(darkTheme = isDarkTheme) {
                var currentTab by remember { mutableStateOf(ScreenTab.HOME) }
                val snackbarHostState = remember { SnackbarHostState() }
                val scope = rememberCoroutineScope()

                // Dialog states
                var showEditBalanceDialog by remember { mutableStateOf(false) }
                var showAddExpenseDialog by remember { mutableStateOf(false) }
                var editingExpense by remember { mutableStateOf<Expense?>(null) }
                var showAddIncomeDialog by remember { mutableStateOf(false) }
                var editingIncome by remember { mutableStateOf<Income?>(null) }
                var showEditBudgetDialog by remember { mutableStateOf(false) }
                var showAddGoalDialog by remember { mutableStateOf(false) }
                var transferGoal by remember { mutableStateOf<Pair<Boolean, SavingsGoal>?>(null) }
                var showManagePresetsDialog by remember { mutableStateOf(false) }
                var showProfileDialog by remember { mutableStateOf(false) }

                // ViewModel States
                val metrics by viewModel.dashboardMetrics.collectAsState()
                val expenses by viewModel.expenses.collectAsState()
                val incomes by viewModel.incomes.collectAsState()
                val budget by viewModel.budget.collectAsState()
                val savingsGoals by viewModel.savingsGoals.collectAsState()
                val quickPresets by viewModel.quickPresets.collectAsState()
                val userProfile by viewModel.userProfile.collectAsState()
                val currentBalance by viewModel.balance.collectAsState()

                LaunchedEffect(Unit) {
                    viewModel.userMessage.collectLatest { msg ->
                        scope.launch {
                            snackbarHostState.showSnackbar(msg)
                        }
                    }
                }

                Scaffold(
                    snackbarHost = { SnackbarHost(snackbarHostState) },
                    topBar = {
                        CenterAlignedTopAppBar(
                            title = {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Image(
                                        painter = painterResource(id = R.drawable.peso_companion),
                                        contentDescription = "PESO Logo",
                                        modifier = Modifier.size(28.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "PESO",
                                        fontWeight = FontWeight.Black,
                                        letterSpacing = 1.sp,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                    if (currentTab != ScreenTab.HOME) {
                                        Text(
                                            text = " • ${currentTab.title}",
                                            fontWeight = FontWeight.SemiBold,
                                            fontSize = 15.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            },
                            actions = {
                                IconButton(
                                    onClick = { currentTab = ScreenTab.REPORTS },
                                    modifier = Modifier.testTag("btn_top_reports")
                                ) {
                                    Icon(
                                        Icons.Default.BarChart,
                                        contentDescription = "Reports",
                                        tint = if (currentTab == ScreenTab.REPORTS) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                IconButton(
                                    onClick = { showProfileDialog = true },
                                    modifier = Modifier.testTag("btn_top_profile")
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(32.dp)
                                            .clip(CircleShape)
                                            .background(MaterialTheme.colorScheme.surfaceVariant),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            Icons.Default.Person,
                                            contentDescription = "User profile",
                                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                }
                            },
                            colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                                containerColor = MaterialTheme.colorScheme.background
                            )
                        )
                    },
                    bottomBar = {
                        NavigationBar(
                            containerColor = MaterialTheme.colorScheme.surface,
                            modifier = Modifier.testTag("bottom_nav")
                        ) {
                            NavigationBarItem(
                                selected = currentTab == ScreenTab.HOME,
                                onClick = { currentTab = ScreenTab.HOME },
                                icon = { Icon(Icons.Default.Home, contentDescription = "Home") },
                                label = { Text("Home", fontSize = 11.sp) },
                                colors = NavigationBarItemDefaults.colors(
                                    indicatorColor = MaterialTheme.colorScheme.primaryContainer
                                ),
                                modifier = Modifier.testTag("nav_home")
                            )
                            NavigationBarItem(
                                selected = currentTab == ScreenTab.EXPENSES,
                                onClick = { currentTab = ScreenTab.EXPENSES },
                                icon = { Icon(Icons.AutoMirrored.Filled.TrendingDown, contentDescription = "Expenses") },
                                label = { Text("Expenses", fontSize = 11.sp) },
                                colors = NavigationBarItemDefaults.colors(
                                    indicatorColor = MaterialTheme.colorScheme.primaryContainer
                                ),
                                modifier = Modifier.testTag("nav_expenses")
                            )
                            NavigationBarItem(
                                selected = currentTab == ScreenTab.INCOME,
                                onClick = { currentTab = ScreenTab.INCOME },
                                icon = { Icon(Icons.Default.AccountBalanceWallet, contentDescription = "Income") },
                                label = { Text("Income", fontSize = 11.sp) },
                                colors = NavigationBarItemDefaults.colors(
                                    indicatorColor = MaterialTheme.colorScheme.primaryContainer
                                ),
                                modifier = Modifier.testTag("nav_income")
                            )
                            NavigationBarItem(
                                selected = currentTab == ScreenTab.BUDGET,
                                onClick = { currentTab = ScreenTab.BUDGET },
                                icon = { Icon(Icons.Default.PieChart, contentDescription = "Budget") },
                                label = { Text("Budget", fontSize = 11.sp) },
                                colors = NavigationBarItemDefaults.colors(
                                    indicatorColor = MaterialTheme.colorScheme.primaryContainer
                                ),
                                modifier = Modifier.testTag("nav_budget")
                            )
                            NavigationBarItem(
                                selected = currentTab == ScreenTab.GOALS,
                                onClick = { currentTab = ScreenTab.GOALS },
                                icon = { Icon(Icons.Default.TrackChanges, contentDescription = "Savings") },
                                label = { Text("Savings", fontSize = 11.sp) },
                                colors = NavigationBarItemDefaults.colors(
                                    indicatorColor = MaterialTheme.colorScheme.primaryContainer
                                ),
                                modifier = Modifier.testTag("nav_goals")
                            )
                        }
                    }
                ) { innerPadding ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                    ) {
                        when (currentTab) {
                            ScreenTab.HOME -> DashboardScreen(
                                metrics = metrics,
                                presets = quickPresets,
                                recentExpenses = expenses,
                                recentIncomes = incomes,
                                onEditBalance = { showEditBalanceDialog = true },
                                onTriggerQuickSpend = { preset -> viewModel.triggerQuickSpend(preset) },
                                onManagePresets = { showManagePresetsDialog = true },
                                onNavigateExpenses = { currentTab = ScreenTab.EXPENSES },
                                onNavigateIncome = { currentTab = ScreenTab.INCOME },
                                onNavigateBudget = { currentTab = ScreenTab.BUDGET }
                            )

                            ScreenTab.EXPENSES -> ExpensesScreen(
                                expenses = expenses,
                                onAddExpense = { showAddExpenseDialog = true },
                                onEditExpense = { exp -> editingExpense = exp },
                                onDeleteExpense = { exp -> viewModel.deleteExpense(exp) }
                            )

                            ScreenTab.INCOME -> IncomeScreen(
                                incomes = incomes,
                                onAddIncome = { showAddIncomeDialog = true },
                                onEditIncome = { inc -> editingIncome = inc },
                                onDeleteIncome = { inc -> viewModel.deleteIncome(inc) }
                            )

                            ScreenTab.BUDGET -> {
                                val monthKey = PesoDateUtils.currentMonthKey()
                                val monthExpenses = expenses.filter { it.date.startsWith(monthKey) }
                                BudgetScreen(
                                    budget = budget,
                                    thisMonthExpenses = monthExpenses,
                                    onEditBudget = { showEditBudgetDialog = true }
                                )
                            }

                            ScreenTab.GOALS -> SavingsGoalsScreen(
                                goals = savingsGoals,
                                walletBalance = currentBalance,
                                onAddGoal = { showAddGoalDialog = true },
                                onDeposit = { goal -> transferGoal = Pair(true, goal) },
                                onWithdraw = { goal -> transferGoal = Pair(false, goal) },
                                onDeleteGoal = { goal -> viewModel.deleteGoal(goal) }
                            )

                            ScreenTab.REPORTS -> ReportsScreen(
                                expenses = expenses,
                                incomes = incomes
                            )
                        }
                    }
                }

                // Dialogs
                if (showEditBalanceDialog) {
                    EditBalanceDialog(
                        currentBalance = currentBalance,
                        onDismiss = { showEditBalanceDialog = false },
                        onConfirm = { newBal ->
                            viewModel.updateBalance(newBal)
                            showEditBalanceDialog = false
                        }
                    )
                }

                if (showAddExpenseDialog) {
                    AddOrEditExpenseDialog(
                        onDismiss = { showAddExpenseDialog = false },
                        onConfirm = { amount, category, date, note ->
                            viewModel.addExpense(amount, category, date, note)
                            showAddExpenseDialog = false
                        }
                    )
                }

                editingExpense?.let { exp ->
                    AddOrEditExpenseDialog(
                        initialExpense = exp,
                        onDismiss = { editingExpense = null },
                        onConfirm = { amount, category, date, note ->
                            viewModel.updateExpense(exp, amount, category, date, note)
                            editingExpense = null
                        }
                    )
                }

                if (showAddIncomeDialog) {
                    AddOrEditIncomeDialog(
                        onDismiss = { showAddIncomeDialog = false },
                        onConfirm = { amount, source, date, note ->
                            viewModel.addIncome(amount, source, date, note)
                            showAddIncomeDialog = false
                        }
                    )
                }

                editingIncome?.let { inc ->
                    AddOrEditIncomeDialog(
                        initialIncome = inc,
                        onDismiss = { editingIncome = null },
                        onConfirm = { amount, source, date, note ->
                            viewModel.updateIncome(inc, amount, source, date, note)
                            editingIncome = null
                        }
                    )
                }

                if (showEditBudgetDialog) {
                    EditBudgetDialog(
                        currentBudget = budget,
                        onDismiss = { showEditBudgetDialog = false },
                        onConfirm = { total, catLimits ->
                            viewModel.saveBudget(total, catLimits)
                            showEditBudgetDialog = false
                        }
                    )
                }

                if (showAddGoalDialog) {
                    AddGoalDialog(
                        onDismiss = { showAddGoalDialog = false },
                        onConfirm = { name, target, initialDeposit, date ->
                            viewModel.addSavingsGoal(name, target, initialDeposit, date)
                            showAddGoalDialog = false
                        }
                    )
                }

                transferGoal?.let { (isDeposit, goal) ->
                    DepositWithdrawDialog(
                        isDeposit = isDeposit,
                        goal = goal,
                        walletBalance = currentBalance,
                        onDismiss = { transferGoal = null },
                        onConfirm = { amount ->
                            if (isDeposit) {
                                viewModel.depositToGoal(goal, amount)
                            } else {
                                viewModel.withdrawFromGoal(goal, amount)
                            }
                            transferGoal = null
                        }
                    )
                }

                if (showManagePresetsDialog) {
                    ManagePresetsDialog(
                        presets = quickPresets,
                        onDismiss = { showManagePresetsDialog = false },
                        onAddPreset = { label, amount, classification, note ->
                            viewModel.addQuickPreset(label, amount, classification, note)
                        },
                        onDeletePreset = { preset ->
                            viewModel.deleteQuickPreset(preset)
                        },
                        onResetPresets = {
                            viewModel.resetQuickPresets()
                        }
                    )
                }

                if (showProfileDialog) {
                    ProfileDialog(
                        userProfile = userProfile,
                        isDarkTheme = isDarkTheme,
                        onDismiss = { showProfileDialog = false },
                        onToggleTheme = { viewModel.toggleTheme() },
                        onUpdateProfile = { name, email ->
                            viewModel.updateProfile(name, email, isDarkTheme)
                        },
                        onResetData = { viewModel.resetDemoData() },
                        onClearData = { viewModel.clearAllData() }
                    )
                }
            }
        }
    }
}
