# NearAndDear Architecture and Flow

This document explains the current code structure and runtime flow after the latest auth/splash cleanup.

## 1) High-level structure

```text
NearAndDear/
├─ app/
│  └─ src/main/java/com/chun/nearanddear/
│     ├─ data/
│     │  ├─ local/
│     │  │  └─ UserPreferencesManager.kt
│     │  ├─ remote/supabase/
│     │  │  └─ SupabaseAuthDataSource.kt
│     │  └─ repository/
│     │     └─ AuthRepositoryImpl.kt
│     ├─ di/
│     │  ├─ RepositoryModule.kt
│     │  ├─ SupabaseConfig.kt
│     │  └─ SupabaseModule.kt
│     ├─ domain/
│     │  ├─ auth/
│     │  │  ├─ LoginErrorMapper.kt
│     │  │  └─ LoginOutcome.kt
│     │  ├─ model/
│     │  │  └─ User.kt
│     │  ├─ repository/
│     │  │  └─ AuthRepository.kt
│     │  ├─ service/
│     │  │  └─ GoogleAuthService.kt
│     │  └─ usecase/auth/
│     │     ├─ HandleUsesFromPreferences.kt
│     │     └─ LoginUseCase.kt
│     ├─ ui/
│     │  ├─ navigation/
│     │  │  ├─ AppNavHost.kt
│     │  │  └─ Routes.kt
│     │  ├─ screens/
│     │  │  ├─ auth/
│     │  │  │  ├─ AuthUiState.kt
│     │  │  │  ├─ AuthViewModel.kt
│     │  │  │  └─ LoginScreen.kt
│     │  │  ├─ home/
│     │  │  │  └─ HomeScreen.kt
│     │  │  └─ splash/
│     │  │     ├─ SplashScreen.kt
│     │  │     ├─ SplashUiState.kt
│     │  │     └─ SplashViewModel.kt
│     │  └─ utils/
│     ├─ logging/
│     │  └─ CrashFileLogger.kt
│     ├─ MainActivity.kt
│     └─ NearAndDearApp.kt
└─ ARCHITECTURE.md
```

## 2) Layer responsibilities

- `ui`: Compose screens, navigation, and ViewModels.
- `domain`: business contracts (`AuthRepository`), use cases, and auth result mapping.
- `data`: concrete implementations for repository, local preferences, and Supabase API calls.
- `di`: Hilt wiring for repository and Supabase client dependencies.

## 3) App startup flow

1. `NearAndDearApp` starts and installs crash logger.
2. `MainActivity` sets Compose content and loads `AppNavHost`.
3. Navigation starts at `Routes.Auth.SPLASH`.
4. `SplashViewModel` waits for splash delay, checks login state from local preferences.
5. App navigates:
   - logged in -> `Routes.Main.HOME`
   - not logged in -> `Routes.Auth.LOGIN`

## 4) Login flow (Google + Supabase)

1. `LoginScreen` asks `AuthViewModel` to login.
2. `AuthViewModel` calls `LoginUseCase`.
3. `LoginUseCase` delegates to `AuthRepository.loginWithGoogle(context)`.
4. `AuthRepositoryImpl`:
   - gets Google sign-in result via `GoogleAuthService`
   - on success, inserts/reads user in Supabase via `SupabaseAuthDataSource`
   - maps exceptions to user-friendly `LoginOutcome.Failure`
5. `AuthViewModel`:
   - on success: saves user info to preferences (`SaveUserIdUseCase`), emits success state
   - on failure/cancel: emits error/idle state
6. `LoginScreen` navigates to Home when success state is observed.

## 5) Flow check status

### Confirmed

- Kotlin compile succeeds (`:app:compileDebugKotlin`).
- Splash-to-login/home decision now uses a valid boolean check path.
- Removed previously unused/duplicate auth-user files to reduce architectural noise.

### Recommended manual checks

- Cold start when no saved user -> lands on Login.
- Cold start with saved user -> lands on Home.
- Login cancellation path -> returns to idle without crash.
- Login failure path -> shows mapped error title/message.
- Successful login -> user info persisted and next launch goes to Home.

## 6) Current cleanup notes

- `HandleUsesFromPreferences.kt` contains multiple use case classes in one file.
  - Works correctly, but splitting to one class per file will improve readability and maintainability.
- There is a non-blocking deprecation warning in `CrashFileLogger.kt`.

