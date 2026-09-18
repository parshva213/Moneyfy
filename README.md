# 💰 Moneyfy

<p align="center">
  <img src="app/ic_launcher-playstore.png" alt="Moneyfy Logo" width="120" height="120" style="border-radius: 20px;" />
</p>

<p align="center">
  <b>Smart Personal Finance & Multi-Account Expense Manager</b><br/>
  Built with Kotlin, Jetpack Compose & Firebase
</p>

<p align="center">
  <img src="https://img.shields.io/badge/Version-1.0.0-00BFA6?style=for-the-badge" alt="Version" />
  <img src="https://img.shields.io/badge/Android-API%2029%2B-3DDC84?style=for-the-badge&logo=android&logoColor=white" alt="Android API" />
  <img src="https://img.shields.io/badge/Kotlin-2.0.21-7F52FF?style=for-the-badge&logo=kotlin&logoColor=white" alt="Kotlin" />
  <img src="https://img.shields.io/badge/Firebase-Connected-FFCA28?style=for-the-badge&logo=firebase&logoColor=black" alt="Firebase" />
  <img src="https://img.shields.io/badge/SIH-2026%20Edition-FF5252?style=for-the-badge" alt="SIH 2026" />
</p>

---

## 📖 Table of Contents

- [Overview](#-overview)
- [Features](#-features)
- [Screenshots](#-screenshots)
- [Tech Stack](#-tech-stack)
- [Architecture](#-architecture)
- [Project Structure](#-project-structure)
- [Prerequisites](#-prerequisites)
- [Getting Started](#-getting-started)
- [Firebase Setup](#-firebase-setup)
- [Build & Run](#-build--run)
- [Permissions](#-permissions)
- [Data Models](#-data-models)
- [Key Screens](#-key-screens)
- [Contributing](#-contributing)
- [License](#-license)

---

## 🌟 Overview

**Moneyfy** is a modern Android personal finance application designed to give you full control over your money. It supports multi-account management, categorized expense/income tracking, contact-linked transactions, and real-time Firebase synchronization — all wrapped in a stunning dual light/dark theme built entirely with Jetpack Compose.

Built as a **SIH 2026 Edition** project, Moneyfy demonstrates production-quality Android development practices including MVVM architecture, reactive state management via Kotlin Coroutines + StateFlow, and a fully offline-capable Firebase-backed data layer.

---

## ✨ Features

### 💳 Core Financial Management
- **Multi-Account Support** — Manage Cash, Bank, Wallet, Credit Card, and other accounts
- **Transaction Tracking** — Log Income, Expense, and Transfer transactions with rich metadata
- **Category Management** — Custom income/expense categories with icons
- **Contact-Linked Transactions** — Associate transactions with Persons, Merchants, or Organizations
- **Monthly Dashboard** — At-a-glance balance, income vs. expense summary, and recent activity

### 🔄 Real-Time Sync
- **Firebase Realtime Database** — All data synced to the cloud in real time
- **Firebase Firestore** — Document-based structured data storage
- **Offline-First** — Local data persistence ensures the app works without connectivity
- **Default Data Seeding** — Standard accounts and categories auto-seeded on first login

### 🔔 Notifications & Reminders
- **Daily Entry Reminders** — Configurable daily push notification at a user-chosen time
- **System Notifications** — Action feedback via `AppNotificationManager` (replaces legacy toasts)
- **Reminder Persistence** — Reminder preferences stored in SharedPreferences

### 🔐 Authentication & Security
- **Firebase Authentication** — Email/password login and registration
- **Forgot Password** — Password reset via email
- **Secure Sign-Out** — Session cleanup with account deletion support
- **Biometric Hook** — `BiometricAuthManager` utility ready for biometric lock integration

### 📤 Data Export
- **CSV Export** — Export all transactions to `Downloads/Moneyfy/` folder
- **MediaStore API** — Android 10+ scoped storage compliant
- **Legacy Support** — Fallback for Android 9 and below using file system API

### 🎨 UI & Theme
- **Dual Theme** — Seamless Light/Dark mode toggle
- **Jetpack Compose** — 100% declarative UI with Material 3 components
- **Custom Color System** — Teal/Emerald primary palette with semantic financial colors
- **Smooth Animations** — `animateContentSize`, gradient backgrounds, ripple effects
- **Bottom Navigation** — 6-tab main navigation (Dashboard, Transactions, Accounts, Contacts, Categories, Settings)

### ⚙️ Settings & Configuration
- **Theme Toggle** — Switch light/dark mode in-app
- **Currency Selection** — Choose from INR, USD, EUR, GBP, JPY
- **Reminder Config** — Toggle and time-pick daily reminders
- **Data Sync** — Manual Firebase sync trigger
- **About & Help** — Dedicated About and Help & Support screens

---

## 📸 Screenshots

> _Record: `Screen_recording_20260917.mp4` (included in repo root)_

| Dashboard | Transactions | Settings |
|-----------|-------------|----------|
| Balance card with income/expense summary | Full transaction list with filter | Full settings panel with profile, prefs & export |

---

## 🛠️ Tech Stack

| Category | Technology | Version |
|----------|-----------|---------|
| **Language** | Kotlin | 2.0.21 |
| **UI Framework** | Jetpack Compose + Material 3 | BOM 2024.10.01 |
| **Architecture** | MVVM + Coroutines + StateFlow | — |
| **Navigation** | Navigation Compose | 2.8.4 |
| **Backend** | Firebase Authentication | BOM 33.7.0 |
| **Database** | Firebase Realtime DB + Firestore | BOM 33.7.0 |
| **Auth Services** | Google Play Services Auth | 21.2.0 |
| **Build System** | Gradle (KTS) + AGP | 8.7.3 |
| **Min SDK** | Android 10 (API 29) | — |
| **Target SDK** | Android 15 (API 35) | — |
| **JVM Target** | Java 11 | — |

---

## 🏗️ Architecture

Moneyfy follows the **MVVM (Model-View-ViewModel)** pattern with a clear separation of concerns:

```
┌─────────────────────────────────────────────────────────────┐
│                        UI Layer (Compose)                    │
│  DashboardScreen │ TransactionsScreen │ SettingsScreen │ ... │
└─────────────────────────┬───────────────────────────────────┘
                          │ observes StateFlow
┌─────────────────────────▼───────────────────────────────────┐
│                      ViewModel Layer                         │
│  TransactionViewModel │ AccountViewModel │ CategoryViewModel  │
│  ContactViewModel                                            │
└─────────────────────────┬───────────────────────────────────┘
                          │ calls suspend fns / Flow
┌─────────────────────────▼───────────────────────────────────┐
│                     Repository Layer (Firebase)              │
│  TransactionRepository │ AccountRepository │ AuthRepository  │
│  CategoryRepository    │ ContactRepository                   │
└─────────────────────────┬───────────────────────────────────┘
                          │
┌─────────────────────────▼───────────────────────────────────┐
│              Firebase Backend                                │
│  Firebase Auth │ Firestore │ Realtime Database               │
└─────────────────────────────────────────────────────────────┘
```

**Key patterns used:**
- **StateFlow** for reactive UI state
- **Kotlin Coroutines** for all async operations
- **Repository pattern** to abstract Firebase data sources
- **Sealed classes** for navigation routes (`Screen`)
- **Singleton objects** for utilities (`AppNotificationManager`, `BiometricAuthManager`)

---

## 📁 Project Structure

```
Moneyfy/
├── app/
│   └── src/main/
│       ├── AndroidManifest.xml
│       └── java/com/moneyfy/
│           ├── MoneyfyApplication.kt        # App entry, notification channel init
│           ├── LoginActivity.kt             # Email/password login screen
│           ├── RegisterActivity.kt          # New account registration
│           ├── ForgotPasswordActivity.kt    # Password reset trigger
│           ├── MainActivity.kt              # Compose host activity
│           │
│           ├── data/                        # Domain models
│           │   ├── Models.kt                # Enums: TransactionType, AccountType, etc.
│           │   ├── Transaction.kt
│           │   ├── Account.kt
│           │   ├── Category.kt
│           │   ├── Contact.kt
│           │   ├── TransactionWithDetails.kt
│           │   └── UserProfile.kt
│           │
│           ├── firebase/                    # Data layer
│           │   ├── FirebaseProvider.kt      # Firebase SDK initialization
│           │   ├── AuthRepository.kt        # Auth operations
│           │   ├── TransactionRepository.kt
│           │   ├── AccountRepository.kt
│           │   ├── CategoryRepository.kt
│           │   ├── ContactRepository.kt
│           │   ├── FirestoreMappers.kt      # Model ↔ Firestore mapping
│           │   └── DefaultDataSeeder.kt     # Seed default accounts/categories
│           │
│           ├── ui/
│           │   ├── navigation/
│           │   │   ├── Screen.kt            # Sealed class route definitions
│           │   │   └── BottomNavigation.kt
│           │   ├── screens/
│           │   │   ├── MainScreen.kt        # NavHost + bottom bar scaffold
│           │   │   ├── DashboardScreen.kt   # Balance card + recent transactions
│           │   │   ├── TransactionsScreen.kt
│           │   │   ├── AccountsScreen.kt
│           │   │   ├── CategoriesScreen.kt
│           │   │   ├── ContactsScreen.kt
│           │   │   ├── AddTransactionScreen.kt
│           │   │   ├── SettingsScreen.kt    # Full settings with CSV export
│           │   │   ├── AboutScreen.kt
│           │   │   └── HelpSupportScreen.kt
│           │   ├── theme/
│           │   │   ├── Color.kt             # Brand + semantic color tokens
│           │   │   ├── Theme.kt             # Light/Dark MaterialTheme wrapper
│           │   │   └── Type.kt              # Typography scale
│           │   └── viewmodel/
│           │       ├── TransactionViewModel.kt
│           │       ├── AccountViewModel.kt
│           │       ├── CategoryViewModel.kt
│           │       └── ContactViewModel.kt
│           │
│           └── util/
│               ├── AppNotificationManager.kt   # System notification utility
│               └── BiometricAuthManager.kt     # Biometric + reminder prefs
│
├── build.gradle.kts                # Root build config
├── settings.gradle.kts             # Module settings
├── gradle/
│   ├── libs.versions.toml          # Version catalog
│   └── wrapper/                    # Gradle wrapper
├── database.rules.json             # Firebase Realtime DB security rules
└── google-services.json            # Firebase project config (per-app)
```

---

## 📋 Prerequisites

| Requirement | Version |
|-------------|---------|
| Android Studio | Hedgehog (2023.1.1) or newer |
| JDK | 17 or newer |
| Android SDK | API 29 – 35 |
| Kotlin Plugin | 2.0+ |
| Firebase Project | Active (see setup below) |
| Android Device / Emulator | API 29+ |

---

## 🚀 Getting Started

### 1. Clone the Repository

```bash
git clone https://github.com/<your-username>/Moneyfy.git
cd Moneyfy
```

### 2. Open in Android Studio

1. Launch **Android Studio**
2. Select **File → Open** and navigate to the cloned `Moneyfy/` folder
3. Wait for Gradle to sync (may take 1–3 minutes on first run)

### 3. Configure Firebase

See [Firebase Setup](#-firebase-setup) below.

### 4. Build & Run

Connect a physical device or start an emulator (API 29+), then click **▶ Run** in Android Studio or use the CLI:

```bash
./gradlew assembleDebug
```

Install the APK:

```bash
adb install app/build/outputs/apk/debug/app-debug.apk
```

---

## 🔥 Firebase Setup

Moneyfy requires a Firebase project for authentication, Firestore, and Realtime Database.

### Step 1: Create a Firebase Project
1. Go to [Firebase Console](https://console.firebase.google.com/)
2. Click **Add Project** and follow the wizard

### Step 2: Register Your Android App
1. In the Firebase console, click **Add App → Android**
2. Enter package name: `com.moneyfy`
3. Download the generated `google-services.json`
4. Replace `app/google-services.json` in the project with your downloaded file

### Step 3: Enable Services
In your Firebase project:
- **Authentication** → Sign-in method → Enable **Email/Password**
- **Firestore Database** → Create database (start in test mode or production)
- **Realtime Database** → Create database

### Step 4: Apply Security Rules
Apply the included `database.rules.json` to your Realtime Database:
```bash
firebase deploy --only database
```
Or paste the rules manually in **Realtime Database → Rules** in the Firebase Console.

---

## 🔨 Build & Run

### Debug Build
```bash
./gradlew assembleDebug
```

### Release Build
```bash
./gradlew assembleRelease
```
> ⚠️ You will need a signing keystore configured in `build.gradle.kts` for a production release.

### Run Unit Tests
```bash
./gradlew test
```

### Run Instrumentation Tests
```bash
./gradlew connectedAndroidTest
```

### Clean Build
```bash
./gradlew clean
```

---

## 🔒 Permissions

| Permission | Purpose |
|------------|---------|
| `INTERNET` | Firebase sync, authentication |
| `ACCESS_NETWORK_STATE` | Check connectivity before sync |
| `POST_NOTIFICATIONS` | Daily reminders, action feedback notifications (Android 13+) |

> **Note:** On Android 13+ (API 33+), the user will be prompted to grant the `POST_NOTIFICATIONS` permission at runtime. The app degrades gracefully if the permission is denied.

---

## 🗃️ Data Models

### Transaction
```kotlin
data class Transaction(
    val id: String,
    val type: TransactionType,    // INCOME | EXPENSE | TRANSFER
    val title: String,
    val amount: Double,
    val date: Long,               // Unix timestamp
    val accountId: String,
    val categoryId: String?,
    val contactId: String?,
    val notes: String?
)
```

### Account
```kotlin
data class Account(
    val id: String,
    val name: String,
    val type: AccountType,        // CASH | BANK | WALLET | CREDIT_CARD | OTHER
    val balance: Double,
    val currency: String
)
```

### Category
```kotlin
data class Category(
    val id: String,
    val name: String,
    val type: CategoryType,       // INCOME | EXPENSE
    val iconName: String
)
```

### Contact
```kotlin
data class Contact(
    val id: String,
    val name: String,
    val type: ContactType,        // PERSON | MERCHANT | ORGANIZATION
    val phone: String?,
    val email: String?
)
```

---

## 📱 Key Screens

| Screen | Route | Description |
|--------|-------|-------------|
| **Login** | — | Email/password sign-in (entry point) |
| **Register** | — | New user account creation |
| **Forgot Password** | — | Sends password reset email |
| **Dashboard** | `dashboard` | Monthly balance, income/expense totals, recent 10 transactions |
| **Transactions** | `transactions` | Full paginated transaction history with delete support |
| **Add Transaction** | `add_transaction` | Form to log income, expense, or transfer |
| **Accounts** | `accounts` | List and manage financial accounts |
| **Categories** | `categories` | Manage income and expense categories |
| **Contacts** | `contacts` | Manage people/merchants linked to transactions |
| **Settings** | `settings` | Theme, currency, reminders, CSV export, security |
| **About** | `about` | App version, tech stack, credits |
| **Help & Support** | `help_support` | FAQs and support contact |

---

## 🤝 Contributing

1. Fork the repository
2. Create your feature branch: `git checkout -b feature/your-feature-name`
3. Commit your changes: `git commit -m 'feat: add some amazing feature'`
4. Push to the branch: `git push origin feature/your-feature-name`
5. Open a Pull Request

### Code Style
- Follow [Kotlin Coding Conventions](https://kotlinlang.org/docs/coding-conventions.html)
- Use descriptive commit messages (preferably [Conventional Commits](https://www.conventionalcommits.org/))
- Keep composables small and focused — extract reusable components

---

## 📄 License

```
Copyright © 2026 Moneyfy Team. All rights reserved.

Designed and Developed for SIH 2026.
```

---

<p align="center">Made with ❤️ using <b>Kotlin</b> & <b>Jetpack Compose</b></p>
