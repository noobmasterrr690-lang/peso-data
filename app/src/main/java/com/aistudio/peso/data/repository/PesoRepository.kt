package com.aistudio.peso.data.repository

import android.content.Context
import com.aistudio.peso.data.db.PesoDatabaseHelper
import com.aistudio.peso.data.model.Budget
import com.aistudio.peso.data.model.Expense
import com.aistudio.peso.data.model.Income
import com.aistudio.peso.data.model.QuickPreset
import com.aistudio.peso.data.model.SavingsGoal
import com.aistudio.peso.data.model.UserProfile
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext

class PesoRepository(
    context: Context,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) {
    private val dbHelper = PesoDatabaseHelper(context.applicationContext)

    private val _balance = MutableStateFlow(0.0)
    val balance: StateFlow<Double> = _balance.asStateFlow()

    private val _expenses = MutableStateFlow<List<Expense>>(emptyList())
    val expenses: StateFlow<List<Expense>> = _expenses.asStateFlow()

    private val _incomes = MutableStateFlow<List<Income>>(emptyList())
    val incomes: StateFlow<List<Income>> = _incomes.asStateFlow()

    private val _budget = MutableStateFlow(Budget(totalLimit = 3500.0))
    val budget: StateFlow<Budget> = _budget.asStateFlow()

    private val _savingsGoals = MutableStateFlow<List<SavingsGoal>>(emptyList())
    val savingsGoals: StateFlow<List<SavingsGoal>> = _savingsGoals.asStateFlow()

    private val _quickPresets = MutableStateFlow<List<QuickPreset>>(emptyList())
    val quickPresets: StateFlow<List<QuickPreset>> = _quickPresets.asStateFlow()

    private val _userProfile = MutableStateFlow(UserProfile())
    val userProfile: StateFlow<UserProfile> = _userProfile.asStateFlow()

    suspend fun refreshAll() = withContext(ioDispatcher) {
        _balance.value = dbHelper.getBalance()
        _expenses.value = dbHelper.getAllExpenses()
        _incomes.value = dbHelper.getAllIncomes()
        _budget.value = dbHelper.getBudget()
        _savingsGoals.value = dbHelper.getAllGoals()
        _quickPresets.value = dbHelper.getAllPresets()
        _userProfile.value = dbHelper.getProfile()
    }

    suspend fun updateBalance(newBalance: Double) = withContext(ioDispatcher) {
        dbHelper.setBalance(newBalance)
        _balance.value = newBalance
    }

    suspend fun addExpense(expense: Expense): Long = withContext(ioDispatcher) {
        val id = dbHelper.insertExpense(expense)
        val currentBal = dbHelper.getBalance()
        val updatedBal = currentBal - expense.amount
        dbHelper.setBalance(updatedBal)
        _balance.value = updatedBal
        _expenses.value = dbHelper.getAllExpenses()
        id
    }

    suspend fun updateExpense(oldExpense: Expense, newExpense: Expense) = withContext(ioDispatcher) {
        dbHelper.updateExpense(newExpense)
        val currentBal = dbHelper.getBalance()
        val difference = oldExpense.amount - newExpense.amount
        val updatedBal = currentBal + difference
        dbHelper.setBalance(updatedBal)
        _balance.value = updatedBal
        _expenses.value = dbHelper.getAllExpenses()
    }

    suspend fun deleteExpense(expense: Expense) = withContext(ioDispatcher) {
        dbHelper.deleteExpense(expense.id)
        val currentBal = dbHelper.getBalance()
        val updatedBal = currentBal + expense.amount
        dbHelper.setBalance(updatedBal)
        _balance.value = updatedBal
        _expenses.value = dbHelper.getAllExpenses()
    }

    suspend fun addIncome(income: Income): Long = withContext(ioDispatcher) {
        val id = dbHelper.insertIncome(income)
        val currentBal = dbHelper.getBalance()
        val updatedBal = currentBal + income.amount
        dbHelper.setBalance(updatedBal)
        _balance.value = updatedBal
        _incomes.value = dbHelper.getAllIncomes()
        id
    }

    suspend fun updateIncome(oldIncome: Income, newIncome: Income) = withContext(ioDispatcher) {
        dbHelper.updateIncome(newIncome)
        val currentBal = dbHelper.getBalance()
        val difference = newIncome.amount - oldIncome.amount
        val updatedBal = currentBal + difference
        dbHelper.setBalance(updatedBal)
        _balance.value = updatedBal
        _incomes.value = dbHelper.getAllIncomes()
    }

    suspend fun deleteIncome(income: Income) = withContext(ioDispatcher) {
        dbHelper.deleteIncome(income.id)
        val currentBal = dbHelper.getBalance()
        val updatedBal = currentBal - income.amount
        dbHelper.setBalance(updatedBal)
        _balance.value = updatedBal
        _incomes.value = dbHelper.getAllIncomes()
    }

    suspend fun saveBudget(totalLimit: Double, categoryLimits: Map<String, Double>) = withContext(ioDispatcher) {
        dbHelper.saveBudget(totalLimit, categoryLimits)
        _budget.value = dbHelper.getBudget()
    }

    suspend fun addSavingsGoal(goal: SavingsGoal): Long = withContext(ioDispatcher) {
        val id = dbHelper.insertGoal(goal)
        _savingsGoals.value = dbHelper.getAllGoals()
        id
    }

    suspend fun updateSavingsGoal(goal: SavingsGoal) = withContext(ioDispatcher) {
        dbHelper.updateGoal(goal)
        _savingsGoals.value = dbHelper.getAllGoals()
    }

    suspend fun deleteSavingsGoal(id: Long) = withContext(ioDispatcher) {
        dbHelper.deleteGoal(id)
        _savingsGoals.value = dbHelper.getAllGoals()
    }

    suspend fun depositToGoal(goal: SavingsGoal, amount: Double): Boolean = withContext(ioDispatcher) {
        if (amount <= 0) return@withContext false
        val currentBal = dbHelper.getBalance()
        if (currentBal < amount) return@withContext false // cannot deposit more than balance

        val newBal = currentBal - amount
        dbHelper.setBalance(newBal)
        val updatedGoal = goal.copy(currentAmount = goal.currentAmount + amount)
        dbHelper.updateGoal(updatedGoal)

        _balance.value = newBal
        _savingsGoals.value = dbHelper.getAllGoals()
        true
    }

    suspend fun withdrawFromGoal(goal: SavingsGoal, amount: Double): Boolean = withContext(ioDispatcher) {
        if (amount <= 0 || goal.currentAmount < amount) return@withContext false

        val currentBal = dbHelper.getBalance()
        val newBal = currentBal + amount
        dbHelper.setBalance(newBal)
        val updatedGoal = goal.copy(currentAmount = (goal.currentAmount - amount).coerceAtLeast(0.0))
        dbHelper.updateGoal(updatedGoal)

        _balance.value = newBal
        _savingsGoals.value = dbHelper.getAllGoals()
        true
    }

    suspend fun addQuickPreset(preset: QuickPreset): Long = withContext(ioDispatcher) {
        val id = dbHelper.insertPreset(preset)
        _quickPresets.value = dbHelper.getAllPresets()
        id
    }

    suspend fun deleteQuickPreset(id: Long) = withContext(ioDispatcher) {
        dbHelper.deletePreset(id)
        _quickPresets.value = dbHelper.getAllPresets()
    }

    suspend fun resetQuickPresets() = withContext(ioDispatcher) {
        dbHelper.resetPresets()
        _quickPresets.value = dbHelper.getAllPresets()
    }

    suspend fun saveProfile(profile: UserProfile) = withContext(ioDispatcher) {
        dbHelper.saveProfile(profile)
        _userProfile.value = profile
    }

    suspend fun clearAllData() = withContext(ioDispatcher) {
        dbHelper.clearAllData()
        refreshAll()
    }

    suspend fun resetToDemoData() = withContext(ioDispatcher) {
        dbHelper.resetToDemoData()
        refreshAll()
    }
}
