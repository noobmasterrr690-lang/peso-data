# PESO — Personal Expense & Spending Organizer (Android)

**PESO** is a modern, student-friendly personal finance and expense organizer built natively for Android using **Kotlin** and **Jetpack Compose**. Designed specifically for students and young adults in the Philippines, PESO makes daily money management effortless with 1-tap Filipino expense shortcuts, dynamic safe-to-spend allowance pacing, monthly budgets, savings goals with wallet transfers, and the friendly "Peso Companion" mascot.

---

## 📱 Key Features

- **Active Wallet Balance Tracking**
  - Instant wallet balance overview in Philippine Pesos (₱).
  - Quick "Adjust" button to align cash in hand.
  - Automatically updates when expenses, incomes, or savings goal transfers are made.

- **1-Tap Quick Spend Shortcuts**
  - Instant 1-tap buttons for common Filipino student expenditures:
    - **Fare**: Jeepney fare (₱15), Tricycle / Bus (₱25)
    - **Meal**: Student meal (₱75), Merienda / snack (₱35)
    - **Drink**: Coffee / drink (₱45), Water / beverage (₱20)
    - **School Needs**: Printing / xerox (₱20), School supplies (₱30)
    - **Others**: Mobile prepaid load (₱50)
  - Customizable shortcuts: add custom 1-tap shortcuts or reset to default presets anytime.

- **Expense Management**
  - Categorized expenses: Food 🍔, Transport 🚌, Entertainment 🎮, Study 📚, Utilities 💡.
  - Instant search by note or category.
  - Grouped chronologically (Today, Yesterday, Date).
  - Add, edit, and delete expenses with automatic balance restoration.

- **Income Tracker**
  - Record cash inflows: Allowance 👪, Part-time Job 💼, Scholarship 🎓, Gift 🎁, Savings 🏦, Other ➕.
  - Search and filter by source.

- **Monthly Budget & Safe Daily Allowance**
  - Monthly overall spending limits with visual progress indicator (Green <75%, Caution 75–99%, Exceeded ≥100%).
  - Real-time **Safe Daily Spend** calculation (`remaining budget ÷ days left in month`).
  - Per-category spending caps.

- **Savings Goals with Active Wallet Transfers**
  - Create savings goals (e.g., Emergency Fund, New Laptop, Tuition).
  - **Deposit** moves funds from your wallet directly into the goal.
  - **Withdraw** returns funds from the goal back to your wallet.
  - Real-time progress bars and target completion percentages.

- **Reports & Cash Flow Analytics**
  - Monthly cash flow summary (Total Inflow vs. Total Outflow, Net Cashflow).
  - Category spending breakdown with percentages.
  - Top spending category callout and average daily spend metric.

- **Peso Companion Mascot**
  - Contextual financial tips and companion dialogue based on your spending health, safe allowance, and budget limits.

- **Material Design 3 & Theming**
  - Built-in Dark Mode (Volt Green `#D7FF3F` on deep charcoal) and Light Mode (Emerald `#16A34A` on clean slate).
  - Adaptive Android launcher icons and custom drawables.

---

## 🛠️ Technology Stack

- **Platform:** Android (minSdk 26, targetSdk 36)
- **Language:** Kotlin 2.2.10
- **UI Toolkit:** Jetpack Compose + Material Design 3
- **Architecture:** MVVM + Clean Repository Pattern with Kotlin Coroutines & StateFlow
- **Local Persistence:** Android SQLite database (`PesoDatabaseHelper`) with thread-safe reactive repository
- **Build System:** Gradle 9.3.1 with Android Gradle Plugin (AGP) 9.1.1
