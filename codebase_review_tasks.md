# Codebase Review: Proposed Fix Tasks

## 1) Typo fix task
- **Issue:** The hardcoded terms text in `TermsText` uses lowercase “continue” while all call-to-action labels in the same screen use title case (“Continue with Google/Facebook”), creating inconsistent UI copy.
- **Task:** Update the terms string to use consistent capitalization and move it to `strings.xml` for localization.
- **Location:** `app/src/main/java/com/chun/nearanddear/ui/screens/auth/LoginScreen.kt`

## 2) Bug fix task
- **Issue:** `TermsText` toggles `showPrivacyDialog` to `true`, but no privacy dialog is rendered in `LoginScreen`; this click path is currently a no-op from the user’s perspective.
- **Task:** Implement and render a privacy/terms dialog (or navigate to a dedicated screen) when the terms text is clicked.
- **Location:** `app/src/main/java/com/chun/nearanddear/ui/screens/auth/LoginScreen.kt`

## 3) Code comment / documentation discrepancy task
- **Issue:** `SignInButton` KDoc documents params in an order that does not match the function signature and misses `textColor` in the list ordering context, which can mislead future edits.
- **Task:** Rewrite the KDoc so parameter descriptions match the actual signature exactly (`iconResId`, `text`, `textColor`, `backgroundColor`, `onClick`, `isEnabled`) and clarify button behavior.
- **Location:** `app/src/main/java/com/chun/nearanddear/ui/screens/auth/LoginScreen.kt`

## 4) Test improvement task
- **Issue:** The only unit test is a placeholder math assertion and does not validate app behavior.
- **Task:** Replace/add tests around `LoginUseCase` and `AuthViewModel` login state transitions (loading, success, cancel, failure) with fake repository outcomes.
- **Location:** `app/src/test/java/com/chun/nearanddear/ExampleUnitTest.kt`, `app/src/main/java/com/chun/nearanddear/domain/usecase/auth/LoginUseCase.kt`, `app/src/main/java/com/chun/nearanddear/ui/screens/auth/AuthViewModel.kt`
