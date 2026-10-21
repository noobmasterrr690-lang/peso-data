package com.aistudio.peso.data.db

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.aistudio.peso.data.model.Budget
import com.aistudio.peso.data.model.Expense
import com.aistudio.peso.data.model.Income
import com.aistudio.peso.data.model.PesoConstants
import com.aistudio.peso.data.model.PesoDateUtils
import com.aistudio.peso.data.model.QuickPreset
import com.aistudio.peso.data.model.SavingsGoal
import com.aistudio.peso.data.model.UserProfile
import org.json.JSONObject

class PesoDatabaseHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        const val DATABASE_NAME = "peso_local.db"
        const val DATABASE_VERSION = 1

        private const val TABLE_EXPENSES = "expenses"
        private const val TABLE_INCOMES = "incomes"
        private const val TABLE_BALANCE = "balance"
        private const val TABLE_BUDGET = "budgets"
        private const val TABLE_GOALS = "savings_goals"
        private const val TABLE_PRESETS = "quick_presets"
        private const val TABLE_PROFILE = "user_profile"
    }

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE $TABLE_EXPENSES (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                amount REAL NOT NULL,
                category TEXT NOT NULL,
                date TEXT NOT NULL,
                note TEXT,
                created_at INTEGER NOT NULL
            )
            """.trimIndent()
        )

        db.execSQL(
            """
            CREATE TABLE $TABLE_INCOMES (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                amount REAL NOT NULL,
                source TEXT NOT NULL,
                date TEXT NOT NULL,
                note TEXT,
                created_at INTEGER NOT NULL
            )
            """.trimIndent()
        )

        db.execSQL(
            """
            CREATE TABLE $TABLE_BALANCE (
                id INTEGER PRIMARY KEY,
                current_amount REAL NOT NULL,
                updated_at INTEGER NOT NULL
            )
            """.trimIndent()
        )

        db.execSQL(
            """
            CREATE TABLE $TABLE_BUDGET (
                id INTEGER PRIMARY KEY,
                total_limit REAL NOT NULL,
                category_limits TEXT NOT NULL,
                updated_at INTEGER NOT NULL
            )
            """.trimIndent()
        )

        db.execSQL(
            """
            CREATE TABLE $TABLE_GOALS (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                name TEXT NOT NULL,
                target_amount REAL NOT NULL,
                current_amount REAL NOT NULL,
                target_date TEXT,
                created_at INTEGER NOT NULL
            )
            """.trimIndent()
        )

        db.execSQL(
            """
            CREATE TABLE $TABLE_PRESETS (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                label TEXT NOT NULL,
                amount REAL NOT NULL,
                classification TEXT NOT NULL,
                category TEXT NOT NULL,
                note TEXT
            )
            """.trimIndent()
        )

        db.execSQL(
            """
            CREATE TABLE $TABLE_PROFILE (
                id INTEGER PRIMARY KEY,
                name TEXT NOT NULL,
                email TEXT NOT NULL,
                is_dark_mode INTEGER NOT NULL
            )
            """.trimIndent()
        )

        seedInitialData(db)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $TABLE_EXPENSES")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_INCOMES")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_BALANCE")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_BUDGET")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_GOALS")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_PRESETS")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_PROFILE")
        onCreate(db)
    }

    private fun seedInitialData(db: SQLiteDatabase) {
        // Balance
        val balanceValues = ContentValues().apply {
            put("id", 1)
            put("current_amount", 1250.0)
            put("updated_at", System.currentTimeMillis())
        }
        db.insert(TABLE_BALANCE, null, balanceValues)

        // Default Budget
        val defaultCategoryLimits = JSONObject().apply {
            put("Food", 1500.0)
            put("Transport", 800.0)
            put("Entertainment", 400.0)
            put("Study", 500.0)
            put("Utilities", 300.0)
        }
        val budgetValues = ContentValues().apply {
            put("id", 1)
            put("total_limit", 3500.0)
            put("category_limits", defaultCategoryLimits.toString())
            put("updated_at", System.currentTimeMillis())
        }
        db.insert(TABLE_BUDGET, null, budgetValues)

        // User Profile
        val profileValues = ContentValues().apply {
            put("id", 1)
            put("name", "Student")
            put("email", "student@peso.ph")
            put("is_dark_mode", 1)
        }
        db.insert(TABLE_PROFILE, null, profileValues)

        // Quick Presets
        for (preset in PesoConstants.DEFAULT_QUICK_PRESETS) {
            val cv = ContentValues().apply {
                put("label", preset.label)
                put("amount", preset.amount)
                put("classification", preset.classification)
                put("category", preset.category)
                put("note", preset.note)
            }
            db.insert(TABLE_PRESETS, null, cv)
        }

        // Savings Goals
        val g1 = ContentValues().apply {
            put("name", "Emergency Fund")
            put("target_amount", 5000.0)
            put("current_amount", 1500.0)
            put("target_date", "2026-12-31")
            put("created_at", System.currentTimeMillis())
        }
        db.insert(TABLE_GOALS, null, g1)

        val g2 = ContentValues().apply {
            put("name", "New Study Laptop")
            put("target_amount", 25000.0)
            put("current_amount", 4200.0)
            put("target_date", "2027-06-30")
            put("created_at", System.currentTimeMillis())
        }
        db.insert(TABLE_GOALS, null, g2)

        // Sample Incomes
        val today = PesoDateUtils.todayKey()
        val inc1 = ContentValues().apply {
            put("amount", 2500.0)
            put("source", "Allowance")
            put("date", today)
            put("note", "Bi-weekly allowance from parents")
            put("created_at", System.currentTimeMillis() - 86400000L * 2)
        }
        db.insert(TABLE_INCOMES, null, inc1)

        val inc2 = ContentValues().apply {
            put("amount", 1000.0)
            put("source", "Part-time Job")
            put("date", today)
            put("note", "Campus tutoring shift")
            put("created_at", System.currentTimeMillis() - 86400000L * 5)
        }
        db.insert(TABLE_INCOMES, null, inc2)

        // Sample Expenses
        val exp1 = ContentValues().apply {
            put("amount", 75.0)
            put("category", "Food")
            put("date", today)
            put("note", "Campus cafeteria lunch")
            put("created_at", System.currentTimeMillis() - 3600000L)
        }
        db.insert(TABLE_EXPENSES, null, exp1)

        val exp2 = ContentValues().apply {
            put("amount", 25.0)
            put("category", "Transport")
            put("date", today)
            put("note", "Tricycle ride to dorm")
            put("created_at", System.currentTimeMillis() - 14400000L)
        }
        db.insert(TABLE_EXPENSES, null, exp2)

        val exp3 = ContentValues().apply {
            put("amount", 120.0)
            put("category", "Study")
            put("date", today)
            put("note", "Midterm study materials & photocopying")
            put("created_at", System.currentTimeMillis() - 86400000L)
        }
        db.insert(TABLE_EXPENSES, null, exp3)
    }

    /* ---------------- Expenses ---------------- */
    @Synchronized
    fun getAllExpenses(): List<Expense> {
        val list = mutableListOf<Expense>()
        val db = readableDatabase
        val cursor = db.query(TABLE_EXPENSES, null, null, null, null, null, "date DESC, created_at DESC")
        cursor.use {
            while (it.moveToNext()) {
                list.add(
                    Expense(
                        id = it.getLong(it.getColumnIndexOrThrow("id")),
                        amount = it.getDouble(it.getColumnIndexOrThrow("amount")),
                        category = it.getString(it.getColumnIndexOrThrow("category")),
                        date = it.getString(it.getColumnIndexOrThrow("date")),
                        note = it.getString(it.getColumnIndexOrThrow("note")),
                        createdAt = it.getLong(it.getColumnIndexOrThrow("created_at"))
                    )
                )
            }
        }
        return list
    }

    @Synchronized
    fun insertExpense(expense: Expense): Long {
        val db = writableDatabase
        val values = ContentValues().apply {
            put("amount", expense.amount)
            put("category", expense.category)
            put("date", expense.date)
            put("note", expense.note)
            put("created_at", expense.createdAt)
        }
        return db.insert(TABLE_EXPENSES, null, values)
    }

    @Synchronized
    fun updateExpense(expense: Expense): Int {
        val db = writableDatabase
        val values = ContentValues().apply {
            put("amount", expense.amount)
            put("category", expense.category)
            put("date", expense.date)
            put("note", expense.note)
        }
        return db.update(TABLE_EXPENSES, values, "id = ?", arrayOf(expense.id.toString()))
    }

    @Synchronized
    fun deleteExpense(id: Long): Int {
        val db = writableDatabase
        return db.delete(TABLE_EXPENSES, "id = ?", arrayOf(id.toString()))
    }

    /* ---------------- Incomes ---------------- */
    @Synchronized
    fun getAllIncomes(): List<Income> {
        val list = mutableListOf<Income>()
        val db = readableDatabase
        val cursor = db.query(TABLE_INCOMES, null, null, null, null, null, "date DESC, created_at DESC")
        cursor.use {
            while (it.moveToNext()) {
                list.add(
                    Income(
                        id = it.getLong(it.getColumnIndexOrThrow("id")),
                        amount = it.getDouble(it.getColumnIndexOrThrow("amount")),
                        source = it.getString(it.getColumnIndexOrThrow("source")),
                        date = it.getString(it.getColumnIndexOrThrow("date")),
                        note = it.getString(it.getColumnIndexOrThrow("note")),
                        createdAt = it.getLong(it.getColumnIndexOrThrow("created_at"))
                    )
                )
            }
        }
        return list
    }

    @Synchronized
    fun insertIncome(income: Income): Long {
        val db = writableDatabase
        val values = ContentValues().apply {
            put("amount", income.amount)
            put("source", income.source)
            put("date", income.date)
            put("note", income.note)
            put("created_at", income.createdAt)
        }
        return db.insert(TABLE_INCOMES, null, values)
    }

    @Synchronized
    fun updateIncome(income: Income): Int {
        val db = writableDatabase
        val values = ContentValues().apply {
            put("amount", income.amount)
            put("source", income.source)
            put("date", income.date)
            put("note", income.note)
        }
        return db.update(TABLE_INCOMES, values, "id = ?", arrayOf(income.id.toString()))
    }

    @Synchronized
    fun deleteIncome(id: Long): Int {
        val db = writableDatabase
        return db.delete(TABLE_INCOMES, "id = ?", arrayOf(id.toString()))
    }

    /* ---------------- Balance ---------------- */
    @Synchronized
    fun getBalance(): Double {
        val db = readableDatabase
        val cursor = db.query(TABLE_BALANCE, arrayOf("current_amount"), "id = 1", null, null, null, null)
        cursor.use {
            if (it.moveToFirst()) {
                return it.getDouble(0)
            }
        }
        return 0.0
    }

    @Synchronized
    fun setBalance(amount: Double) {
        val db = writableDatabase
        val values = ContentValues().apply {
            put("id", 1)
            put("current_amount", amount)
            put("updated_at", System.currentTimeMillis())
        }
        db.insertWithOnConflict(TABLE_BALANCE, null, values, SQLiteDatabase.CONFLICT_REPLACE)
    }

    /* ---------------- Budget ---------------- */
    @Synchronized
    fun getBudget(): Budget {
        val db = readableDatabase
        val cursor = db.query(TABLE_BUDGET, null, "id = 1", null, null, null, null)
        cursor.use {
            if (it.moveToFirst()) {
                val total = it.getDouble(it.getColumnIndexOrThrow("total_limit"))
                val rawJson = it.getString(it.getColumnIndexOrThrow("category_limits"))
                val map = mutableMapOf<String, Double>()
                if (!rawJson.isNullOrBlank()) {
                    try {
                        val json = JSONObject(rawJson)
                        val keys = json.keys()
                        while (keys.hasNext()) {
                            val k = keys.next()
                            map[k] = json.optDouble(k, 0.0)
                        }
                    } catch (_: Exception) {}
                }
                return Budget(
                    id = 1,
                    totalLimit = total,
                    categoryLimits = map,
                    updatedAt = it.getLong(it.getColumnIndexOrThrow("updated_at"))
                )
            }
        }
        return Budget(id = 1, totalLimit = 3500.0)
    }

    @Synchronized
    fun saveBudget(totalLimit: Double, categoryLimits: Map<String, Double>) {
        val db = writableDatabase
        val json = JSONObject()
        for ((k, v) in categoryLimits) {
            json.put(k, v)
        }
        val values = ContentValues().apply {
            put("id", 1)
            put("total_limit", totalLimit)
            put("category_limits", json.toString())
            put("updated_at", System.currentTimeMillis())
        }
        db.insertWithOnConflict(TABLE_BUDGET, null, values, SQLiteDatabase.CONFLICT_REPLACE)
    }

    /* ---------------- Savings Goals ---------------- */
    @Synchronized
    fun getAllGoals(): List<SavingsGoal> {
        val list = mutableListOf<SavingsGoal>()
        val db = readableDatabase
        val cursor = db.query(TABLE_GOALS, null, null, null, null, null, "created_at DESC")
        cursor.use {
            while (it.moveToNext()) {
                list.add(
                    SavingsGoal(
                        id = it.getLong(it.getColumnIndexOrThrow("id")),
                        name = it.getString(it.getColumnIndexOrThrow("name")),
                        targetAmount = it.getDouble(it.getColumnIndexOrThrow("target_amount")),
                        currentAmount = it.getDouble(it.getColumnIndexOrThrow("current_amount")),
                        targetDate = it.getString(it.getColumnIndexOrThrow("target_date")),
                        createdAt = it.getLong(it.getColumnIndexOrThrow("created_at"))
                    )
                )
            }
        }
        return list
    }

    @Synchronized
    fun insertGoal(goal: SavingsGoal): Long {
        val db = writableDatabase
        val values = ContentValues().apply {
            put("name", goal.name)
            put("target_amount", goal.targetAmount)
            put("current_amount", goal.currentAmount)
            put("target_date", goal.targetDate)
            put("created_at", goal.createdAt)
        }
        return db.insert(TABLE_GOALS, null, values)
    }

    @Synchronized
    fun updateGoal(goal: SavingsGoal): Int {
        val db = writableDatabase
        val values = ContentValues().apply {
            put("name", goal.name)
            put("target_amount", goal.targetAmount)
            put("current_amount", goal.currentAmount)
            put("target_date", goal.targetDate)
        }
        return db.update(TABLE_GOALS, values, "id = ?", arrayOf(goal.id.toString()))
    }

    @Synchronized
    fun deleteGoal(id: Long): Int {
        val db = writableDatabase
        return db.delete(TABLE_GOALS, "id = ?", arrayOf(id.toString()))
    }

    /* ---------------- Quick Presets ---------------- */
    @Synchronized
    fun getAllPresets(): List<QuickPreset> {
        val list = mutableListOf<QuickPreset>()
        val db = readableDatabase
        val cursor = db.query(TABLE_PRESETS, null, null, null, null, null, "id ASC")
        cursor.use {
            while (it.moveToNext()) {
                list.add(
                    QuickPreset(
                        id = it.getLong(it.getColumnIndexOrThrow("id")),
                        label = it.getString(it.getColumnIndexOrThrow("label")),
                        amount = it.getDouble(it.getColumnIndexOrThrow("amount")),
                        classification = it.getString(it.getColumnIndexOrThrow("classification")),
                        category = it.getString(it.getColumnIndexOrThrow("category")),
                        note = it.getString(it.getColumnIndexOrThrow("note"))
                    )
                )
            }
        }
        return list
    }

    @Synchronized
    fun insertPreset(preset: QuickPreset): Long {
        val db = writableDatabase
        val values = ContentValues().apply {
            put("label", preset.label)
            put("amount", preset.amount)
            put("classification", preset.classification)
            put("category", preset.category)
            put("note", preset.note)
        }
        return db.insert(TABLE_PRESETS, null, values)
    }

    @Synchronized
    fun deletePreset(id: Long): Int {
        val db = writableDatabase
        return db.delete(TABLE_PRESETS, "id = ?", arrayOf(id.toString()))
    }

    @Synchronized
    fun resetPresets() {
        val db = writableDatabase
        db.delete(TABLE_PRESETS, null, null)
        for (preset in PesoConstants.DEFAULT_QUICK_PRESETS) {
            val cv = ContentValues().apply {
                put("label", preset.label)
                put("amount", preset.amount)
                put("classification", preset.classification)
                put("category", preset.category)
                put("note", preset.note)
            }
            db.insert(TABLE_PRESETS, null, cv)
        }
    }

    /* ---------------- Profile ---------------- */
    @Synchronized
    fun getProfile(): UserProfile {
        val db = readableDatabase
        val cursor = db.query(TABLE_PROFILE, null, "id = 1", null, null, null, null)
        cursor.use {
            if (it.moveToFirst()) {
                return UserProfile(
                    name = it.getString(it.getColumnIndexOrThrow("name")),
                    email = it.getString(it.getColumnIndexOrThrow("email")),
                    isDarkMode = it.getInt(it.getColumnIndexOrThrow("is_dark_mode")) == 1,
                    currentBalance = getBalance()
                )
            }
        }
        return UserProfile()
    }

    @Synchronized
    fun saveProfile(profile: UserProfile) {
        val db = writableDatabase
        val values = ContentValues().apply {
            put("id", 1)
            put("name", profile.name)
            put("email", profile.email)
            put("is_dark_mode", if (profile.isDarkMode) 1 else 0)
        }
        db.insertWithOnConflict(TABLE_PROFILE, null, values, SQLiteDatabase.CONFLICT_REPLACE)
    }

    @Synchronized
    fun clearAllData() {
        val db = writableDatabase
        db.delete(TABLE_EXPENSES, null, null)
        db.delete(TABLE_INCOMES, null, null)
        db.delete(TABLE_GOALS, null, null)
        setBalance(0.0)
    }

    @Synchronized
    fun resetToDemoData() {
        val db = writableDatabase
        onUpgrade(db, 1, 1)
    }
}
