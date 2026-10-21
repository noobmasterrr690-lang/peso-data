package com.aistudio.peso.data.model

import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

data class Expense(
    val id: Long = 0,
    val amount: Double,
    val category: String,
    val date: String, // YYYY-MM-DD
    val note: String? = null,
    val createdAt: Long = System.currentTimeMillis()
)

data class Income(
    val id: Long = 0,
    val amount: Double,
    val source: String,
    val date: String, // YYYY-MM-DD
    val note: String? = null,
    val createdAt: Long = System.currentTimeMillis()
)

data class Budget(
    val id: Long = 1,
    val totalLimit: Double,
    val categoryLimits: Map<String, Double> = emptyMap(),
    val updatedAt: Long = System.currentTimeMillis()
)

data class SavingsGoal(
    val id: Long = 0,
    val name: String,
    val targetAmount: Double,
    val currentAmount: Double = 0.0,
    val targetDate: String? = null, // YYYY-MM-DD
    val createdAt: Long = System.currentTimeMillis()
) {
    val progressPercent: Float
        get() = if (targetAmount > 0) ((currentAmount / targetAmount) * 100).toFloat().coerceIn(0f, 100f) else 0f

    val isCompleted: Boolean
        get() = currentAmount >= targetAmount && targetAmount > 0
}

data class QuickPreset(
    val id: Long = 0,
    val label: String,
    val amount: Double,
    val classification: String = "Others",
    val category: String = "Utilities",
    val note: String? = null
)

data class UserProfile(
    val name: String = "Student",
    val email: String = "student@peso.app",
    val currentBalance: Double = 500.0,
    val isDarkMode: Boolean = true
)

data class CategoryMeta(
    val id: String,
    val icon: String,
    val hint: String
)

data class ClassificationMeta(
    val id: String,
    val label: String,
    val defaultCategory: String
)

object PesoConstants {
    val CATEGORIES = listOf(
        CategoryMeta("Food", "🍔", "Meals, snacks, drinks, groceries"),
        CategoryMeta("Transport", "🚌", "Jeepney, bus, tricycle, Grab, gas"),
        CategoryMeta("Entertainment", "🎮", "Movies, games, hangouts, subscriptions"),
        CategoryMeta("Study", "📚", "Books, printing, school supplies, fees"),
        CategoryMeta("Utilities", "💡", "Load, internet, bills, laundry")
    )

    fun getCategoryIcon(id: String): String =
        CATEGORIES.find { it.id.equals(id, ignoreCase = true) }?.icon ?: "🧾"

    val INCOME_SOURCES = listOf(
        "Allowance" to "👪",
        "Part-time Job" to "💼",
        "Scholarship" to "🎓",
        "Gift" to "🎁",
        "Savings" to "🏦",
        "Other" to "➕"
    )

    fun getSourceIcon(source: String): String =
        INCOME_SOURCES.find { it.first.equals(source, ignoreCase = true) }?.second ?: "💰"

    val QUICK_CLASSIFICATIONS = listOf(
        ClassificationMeta("Fare", "Fare", "Transport"),
        ClassificationMeta("Meal", "Meal", "Food"),
        ClassificationMeta("Drink", "Drink", "Food"),
        ClassificationMeta("School Needs", "School Needs", "Study"),
        ClassificationMeta("Others", "Others", "Utilities")
    )

    val DEFAULT_QUICK_PRESETS = listOf(
        QuickPreset(label = "Jeepney fare", amount = 15.0, classification = "Fare", category = "Transport", note = "Jeepney fare"),
        QuickPreset(label = "Tricycle / Bus", amount = 25.0, classification = "Fare", category = "Transport", note = "Commute fare"),
        QuickPreset(label = "Student meal", amount = 75.0, classification = "Meal", category = "Food", note = "Campus meal"),
        QuickPreset(label = "Merienda / snack", amount = 35.0, classification = "Meal", category = "Food", note = "Merienda"),
        QuickPreset(label = "Coffee / drink", amount = 45.0, classification = "Drink", category = "Food", note = "Coffee / drink"),
        QuickPreset(label = "Water / beverage", amount = 20.0, classification = "Drink", category = "Food", note = "Water / beverage"),
        QuickPreset(label = "Printing / xerox", amount = 20.0, classification = "School Needs", category = "Study", note = "School printing"),
        QuickPreset(label = "School supplies", amount = 30.0, classification = "School Needs", category = "Study", note = "School supplies"),
        QuickPreset(label = "Mobile load", amount = 50.0, classification = "Others", category = "Utilities", note = "Prepaid load")
    )
}

object PesoDateUtils {
    private val isoFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)
    private val monthFormat = SimpleDateFormat("yyyy-MM", Locale.US)
    private val friendlyMonthFormat = SimpleDateFormat("MMMM yyyy", Locale.US)
    private val friendlyDateFormat = SimpleDateFormat("MMM d, yyyy", Locale.US)

    fun todayKey(): String = isoFormat.format(Date())

    fun currentMonthKey(): String = monthFormat.format(Date())

    fun formatCurrency(amount: Double): String {
        val format = NumberFormat.getCurrencyInstance(Locale("en", "PH"))
        return format.format(amount).replace("PHP", "₱")
    }

    fun formatMonthName(monthKey: String): String {
        return try {
            val date = monthFormat.parse(monthKey) ?: return monthKey
            friendlyMonthFormat.format(date)
        } catch (_: Exception) {
            monthKey
        }
    }

    fun formatFriendlyDate(dateKey: String): String {
        val today = todayKey()
        if (dateKey == today) return "Today"

        val cal = Calendar.getInstance()
        cal.add(Calendar.DAY_OF_YEAR, -1)
        val yesterday = isoFormat.format(cal.time)
        if (dateKey == yesterday) return "Yesterday"

        return try {
            val date = isoFormat.parse(dateKey) ?: return dateKey
            friendlyDateFormat.format(date)
        } catch (_: Exception) {
            dateKey
        }
    }

    fun daysLeftInMonth(): Int {
        val cal = Calendar.getInstance()
        val currentDay = cal.get(Calendar.DAY_OF_MONTH)
        val maxDays = cal.getActualMaximum(Calendar.DAY_OF_MONTH)
        return (maxDays - currentDay + 1).coerceAtLeast(1)
    }

    fun getGreeting(): String {
        val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        return when {
            hour < 12 -> "Good morning"
            hour < 17 -> "Good afternoon"
            else -> "Good evening"
        }
    }
}
