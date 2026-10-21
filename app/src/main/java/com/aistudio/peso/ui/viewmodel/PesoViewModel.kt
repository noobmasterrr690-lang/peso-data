package com.aistudio.peso.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.aistudio.peso.data.model.Budget
import com.aistudio.peso.data.model.Expense
import com.aistudio.peso.data.model.Income
import com.aistudio.peso.data.model.PesoConstants
import com.aistudio.peso.data.model.PesoDateUtils
import com.aistudio.peso.data.model.QuickPreset
import com.aistudio.peso.data.model.SavingsGoal
import com.aistudio.peso.data.model.UserProfile
import com.aistudio.peso.data.repository.PesoRepository
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class DashboardMetrics(
    val balance: Double = 0.0,
    val monthExpensesTotal: Double = 0.0,
    val monthIncomeTotal: Double = 0.0,
    val budgetTotal: Double = 0.0,
    val budgetRemaining: Double = 0.0,
    val budgetPercent: Float = 0f,
    val dailySafeAllowance: Double = 0.0,
    val daysLeft: Int = 1,
    val companionMessage: String = "Let's keep your spending smart and simple!",
    val isBudgetAlert: Boolean = false
)

class PesoViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = PesoRepository(application)

    val balance: StateFlow<Double> = repository.balance
    val expenses: StateFlow<List<Expense>> = repository.expenses
    val incomes: StateFlow<List<Income>> = repository.incomes
    val budget: StateFlow<Budget> = repository.budget
    val savingsGoals: StateFlow<List<SavingsGoal>> = repository.savingsGoals
    val quickPresets: StateFlow<List<QuickPreset>> = repository.quickPresets
    val userProfile: StateFlow<UserProfile> = repository.userProfile

    private val _userMessage = MutableSharedFlow<String>()
    val userMessage: SharedFlow<String> = _userMessage.asSharedFlow()

    private val _isDarkTheme = MutableStateFlow(true)
    val isDarkTheme: StateFlow<Boolean> = _isDarkTheme.asStateFlow()

    init {
        viewModelScope.launch {
            repository.refreshAll()
            _isDarkTheme.value = repository.userProfile.value.isDarkMode
        }
    }

    val dashboardMetrics: StateFlow<DashboardMetrics> = combine(
        balance,
        expenses,
        incomes,
        budget
    ) { currentBalance, allExpenses, allIncomes, currentBudget ->
        val monthKey = PesoDateUtils.currentMonthKey()
        val monthExpenses = allExpenses.filter { it.date.startsWith(monthKey) }
        val monthExpenseSum = monthExpenses.sumOf { it.amount }

        val monthIncomes = allIncomes.filter { it.date.startsWith(monthKey) }
        val monthIncomeSum = monthIncomes.sumOf { it.amount }

        val budgetTotal = currentBudget.totalLimit
        val budgetRemaining = (budgetTotal - monthExpenseSum).coerceAtLeast(0.0)
        val budgetPercent = if (budgetTotal > 0) ((monthExpenseSum / budgetTotal) * 100).toFloat() else 0f

        val daysLeft = PesoDateUtils.daysLeftInMonth()
        val dailySafe = if (budgetTotal > 0) {
            (budgetRemaining / daysLeft).coerceAtLeast(0.0)
        } else {
            (currentBalance / daysLeft).coerceAtLeast(0.0)
        }

        val isAlert = budgetPercent >= 75f

        val companion = when {
            budgetPercent >= 100f -> "Whoa! You've reached 100% of your monthly budget limit. Stick to essentials for now! 🐷"
            budgetPercent >= 80f -> "Heads up! You've used ${budgetPercent.toInt()}% of your budget limit. Spend carefully! ⚠️"
            currentBalance <= 100 -> "Low balance warning! You have ${PesoDateUtils.formatCurrency(currentBalance)} left in your wallet. 👛"
            dailySafe > 100 -> "Nice going! Your safe allowance today is ${PesoDateUtils.formatCurrency(dailySafe)}. Enjoy your day! ✨"
            else -> "Stay on track! Check your 1-tap shortcuts below to log quick jeepney fares & meals. 🎒"
        }

        DashboardMetrics(
            balance = currentBalance,
            monthExpensesTotal = monthExpenseSum,
            monthIncomeTotal = monthIncomeSum,
            budgetTotal = budgetTotal,
            budgetRemaining = budgetRemaining,
            budgetPercent = budgetPercent,
            dailySafeAllowance = dailySafe,
            daysLeft = daysLeft,
            companionMessage = companion,
            isBudgetAlert = isAlert
        )
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        DashboardMetrics()
    )

    fun triggerQuickSpend(preset: QuickPreset) {
        viewModelScope.launch {
            val expense = Expense(
                amount = preset.amount,
                category = preset.category,
                date = PesoDateUtils.todayKey(),
                note = preset.note ?: preset.label
            )
            repository.addExpense(expense)
            _userMessage.emit("Logged ₱${preset.amount.toInt()} for ${preset.label}!")
        }
    }

    fun addExpense(amount: Double, category: String, date: String, note: String?) {
        viewModelScope.launch {
            val expense = Expense(
                amount = amount,
                category = category,
                date = date,
                note = note
            )
            repository.addExpense(expense)
            _userMessage.emit("Expense of ${PesoDateUtils.formatCurrency(amount)} recorded.")
        }
    }

    fun updateExpense(oldExpense: Expense, amount: Double, category: String, date: String, note: String?) {
        viewModelScope.launch {
            val updated = oldExpense.copy(
                amount = amount,
                category = category,
                date = date,
                note = note
            )
            repository.updateExpense(oldExpense, updated)
            _userMessage.emit("Expense updated.")
        }
    }

    fun deleteExpense(expense: Expense) {
        viewModelScope.launch {
            repository.deleteExpense(expense)
            _userMessage.emit("Deleted expense of ${PesoDateUtils.formatCurrency(expense.amount)}.")
        }
    }

    fun addIncome(amount: Double, source: String, date: String, note: String?) {
        viewModelScope.launch {
            val income = Income(
                amount = amount,
                source = source,
                date = date,
                note = note
            )
            repository.addIncome(income)
            _userMessage.emit("Added income of ${PesoDateUtils.formatCurrency(amount)}.")
        }
    }

    fun updateIncome(oldIncome: Income, amount: Double, source: String, date: String, note: String?) {
        viewModelScope.launch {
            val updated = oldIncome.copy(
                amount = amount,
                source = source,
                date = date,
                note = note
            )
            repository.updateIncome(oldIncome, updated)
            _userMessage.emit("Income updated.")
        }
    }

    fun deleteIncome(income: Income) {
        viewModelScope.launch {
            repository.deleteIncome(income)
            _userMessage.emit("Deleted income of ${PesoDateUtils.formatCurrency(income.amount)}.")
        }
    }

    fun updateBalance(newAmount: Double) {
        viewModelScope.launch {
            repository.updateBalance(newAmount)
            _userMessage.emit("Balance set to ${PesoDateUtils.formatCurrency(newAmount)}.")
        }
    }

    fun saveBudget(totalLimit: Double, categoryLimits: Map<String, Double>) {
        viewModelScope.launch {
            repository.saveBudget(totalLimit, categoryLimits)
            _userMessage.emit("Budget updated successfully.")
        }
    }

    fun addSavingsGoal(name: String, targetAmount: Double, initialDeposit: Double, targetDate: String?) {
        viewModelScope.launch {
            val goal = SavingsGoal(
                name = name,
                targetAmount = targetAmount,
                currentAmount = 0.0,
                targetDate = targetDate
            )
            val id = repository.addSavingsGoal(goal)
            if (initialDeposit > 0) {
                repository.depositToGoal(goal.copy(id = id), initialDeposit)
            }
            _userMessage.emit("Created goal: $name!")
        }
    }

    fun depositToGoal(goal: SavingsGoal, amount: Double) {
        viewModelScope.launch {
            val success = repository.depositToGoal(goal, amount)
            if (success) {
                _userMessage.emit("Deposited ${PesoDateUtils.formatCurrency(amount)} into ${goal.name}!")
            } else {
                _userMessage.emit("Not enough balance to deposit ${PesoDateUtils.formatCurrency(amount)}.")
            }
        }
    }

    fun withdrawFromGoal(goal: SavingsGoal, amount: Double) {
        viewModelScope.launch {
            val success = repository.withdrawFromGoal(goal, amount)
            if (success) {
                _userMessage.emit("Withdrew ${PesoDateUtils.formatCurrency(amount)} from ${goal.name} back to wallet.")
            } else {
                _userMessage.emit("Cannot withdraw more than saved amount.")
            }
        }
    }

    fun deleteGoal(goal: SavingsGoal) {
        viewModelScope.launch {
            if (goal.currentAmount > 0) {
                // return saved amount back to wallet balance
                repository.updateBalance(balance.value + goal.currentAmount)
            }
            repository.deleteSavingsGoal(goal.id)
            _userMessage.emit("Deleted goal ${goal.name}.")
        }
    }

    fun addQuickPreset(label: String, amount: Double, classification: String, note: String?) {
        viewModelScope.launch {
            val defaultCat = PesoConstants.QUICK_CLASSIFICATIONS.find { it.id == classification }?.defaultCategory ?: "Utilities"
            val preset = QuickPreset(
                label = label,
                amount = amount,
                classification = classification,
                category = defaultCat,
                note = note ?: label
            )
            repository.addQuickPreset(preset)
            _userMessage.emit("Added 1-tap shortcut \"$label\"!")
        }
    }

    fun deleteQuickPreset(preset: QuickPreset) {
        viewModelScope.launch {
            repository.deleteQuickPreset(preset.id)
            _userMessage.emit("Deleted shortcut \"${preset.label}\".")
        }
    }

    fun resetQuickPresets() {
        viewModelScope.launch {
            repository.resetQuickPresets()
            _userMessage.emit("Reset 1-tap shortcuts to default presets.")
        }
    }

    fun updateProfile(name: String, email: String, isDark: Boolean) {
        viewModelScope.launch {
            val updated = userProfile.value.copy(name = name, email = email, isDarkMode = isDark)
            repository.saveProfile(updated)
            _isDarkTheme.value = isDark
            _userMessage.emit("Profile updated.")
        }
    }

    fun toggleTheme() {
        val next = !_isDarkTheme.value
        _isDarkTheme.value = next
        viewModelScope.launch {
            val updated = userProfile.value.copy(isDarkMode = next)
            repository.saveProfile(updated)
        }
    }

    fun resetDemoData() {
        viewModelScope.launch {
            repository.resetToDemoData()
            _userMessage.emit("Reset all data to student starter sample.")
        }
    }

    fun clearAllData() {
        viewModelScope.launch {
            repository.clearAllData()
            _userMessage.emit("Cleared all transactions and goals.")
        }
    }
}
