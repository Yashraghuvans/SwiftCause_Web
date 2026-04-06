# Android Development Checklist

Quick reference checklist for SwiftCause Android development. Use this before committing code.

---

## 🎯 Pre-Commit Checklist

### ✅ Architecture & Structure
- [ ] Code follows MVVM architecture pattern
- [ ] Business logic is in ViewModels/UseCases, not in Composables
- [ ] UI state is managed via StateFlow/State
- [ ] Data layer is separated from domain layer
- [ ] Repository pattern is used for data operations

### ✅ Internationalization (i18n)
- [ ] **NO hardcoded strings in code** - all text uses `stringResource()`
- [ ] All user-facing text is in `strings.xml`
- [ ] String resources use proper format arguments (`%1$s`, `%1$d`)
- [ ] Plurals are used for countable items
- [ ] Content descriptions use string resources

### ✅ UI & Compose
- [ ] Composables follow single responsibility principle
- [ ] State is hoisted where appropriate
- [ ] Modifiers are passed as parameters (last parameter)
- [ ] Preview annotations added for Composables
- [ ] Colors from theme, not hardcoded
- [ ] Dimensions use `dp`/`sp`, not raw numbers
- [ ] Loading and error states are handled

### ✅ Naming Conventions
- [ ] Composable functions are `PascalCase`
- [ ] Regular functions/variables are `camelCase`
- [ ] Constants are `UPPER_SNAKE_CASE`
- [ ] Files follow naming conventions (ViewModel, Screen, UseCase, etc.)
- [ ] Package structure follows project conventions

### ✅ Accessibility
- [ ] All images have `contentDescription`
- [ ] Clickable elements are at least 48dp
- [ ] Semantic properties added where needed
- [ ] Screen reader tested (if possible)

### ✅ Error Handling
- [ ] Network errors are caught and handled
- [ ] User-friendly error messages shown
- [ ] Retry mechanisms implemented
- [ ] Loading states prevent duplicate calls
- [ ] Edge cases considered (empty lists, null values)

### ✅ Performance
- [ ] Heavy operations in background threads (coroutines)
- [ ] Images loaded asynchronously with Coil
- [ ] Lists use `LazyColumn`/`LazyGrid`
- [ ] Expensive calculations use `remember`
- [ ] No blocking operations on main thread

### ✅ Security
- [ ] No API keys committed to git
- [ ] Sensitive data uses EncryptedSharedPreferences
- [ ] HTTPS used for all network calls
- [ ] User input is validated
- [ ] No hardcoded credentials

### ✅ Testing
- [ ] Unit tests written for ViewModels
- [ ] Unit tests written for UseCases
- [ ] Repository tests use fake implementations
- [ ] Edge cases covered in tests
- [ ] Tests pass successfully

### ✅ Code Quality
- [ ] No compiler warnings
- [ ] No unused imports
- [ ] Code is properly formatted (Ctrl+Alt+L)
- [ ] Comments added for complex logic
- [ ] TODOs documented if work is incomplete
- [ ] No debug/test code left in

### ✅ Git
- [ ] Branch named properly (`feature/`, `fix/`, etc.)
- [ ] Commit message follows Conventional Commits
- [ ] Only relevant files staged
- [ ] `.gitignore` working correctly
- [ ] No sensitive files committed

---

## 🚀 Feature Implementation Checklist

When implementing a new feature:

### 1. Domain Layer
- [ ] Create domain model (entity)
- [ ] Create repository interface
- [ ] Create use case(s)

### 2. Data Layer
- [ ] Create DTO/Response model
- [ ] Create API interface (if needed)
- [ ] Implement repository
- [ ] Create mapper (DTO → Domain)

### 3. Presentation Layer
- [ ] Create UI state sealed class/data class
- [ ] Create ViewModel
- [ ] Create Screen composable
- [ ] Create reusable components
- [ ] Add string resources
- [ ] Handle loading/error/success states

### 4. Testing
- [ ] Write ViewModel tests
- [ ] Write UseCase tests
- [ ] Write Repository tests
- [ ] Write Composable tests (if complex)

### 5. Documentation
- [ ] Add KDoc comments
- [ ] Update README if needed
- [ ] Document any quirks or gotchas

---

## 🔍 Code Review Checklist

For reviewers:

### Architecture
- [ ] Follows MVVM pattern correctly
- [ ] Proper separation of concerns
- [ ] Repository pattern used appropriately
- [ ] No business logic in UI layer

### Code Quality
- [ ] No hardcoded strings
- [ ] Naming conventions followed
- [ ] Code is readable and maintainable
- [ ] No code duplication
- [ ] Proper error handling

### Testing
- [ ] Tests exist and pass
- [ ] Edge cases covered
- [ ] Mocks/fakes used appropriately

### UI/UX
- [ ] Matches design specifications
- [ ] Responsive to different screen sizes
- [ ] Loading states implemented
- [ ] Error states user-friendly
- [ ] Accessibility considered

---

## 📝 Quick Reference

### Import StringResource
```kotlin
import androidx.compose.ui.res.stringResource
```

### Use String Resource
```kotlin
Text(text = stringResource(R.string.donate))
Text(text = stringResource(R.string.goal_amount, formattedAmount))
```

### Collect State in Compose
```kotlin
val uiState by viewModel.uiState.collectAsStateWithLifecycle()
```

### Launch Coroutine in ViewModel
```kotlin
viewModelScope.launch {
    // Your code
}
```

### Handle Result
```kotlin
result.onSuccess { data ->
    // Handle success
}.onFailure { error ->
    // Handle error
}
```

---

## ⚠️ Common Mistakes to Catch

### ❌ Hardcoded String
```kotlin
Text("Donate") // BAD
```
### ✅ String Resource
```kotlin
Text(stringResource(R.string.donate)) // GOOD
```

---

### ❌ Business Logic in Composable
```kotlin
@Composable
fun Screen() {
    val campaigns = getCampaignsFromApi() // BAD
}
```
### ✅ Logic in ViewModel
```kotlin
class ViewModel : ViewModel() {
    init {
        loadCampaigns() // GOOD
    }
}
```

---

### ❌ Hardcoded Color
```kotlin
Text(color = Color(0xFF0E8F5A)) // BAD
```
### ✅ Theme Color
```kotlin
Text(color = MaterialTheme.colorScheme.primary) // GOOD
Text(color = PrimaryGreen) // GOOD (from Color.kt)
```

---

### ❌ No Content Description
```kotlin
Image(painter = painter, contentDescription = null) // BAD
```
### ✅ Proper Content Description
```kotlin
Image(
    painter = painter,
    contentDescription = stringResource(R.string.campaign_image)
) // GOOD
```

---

### ❌ Blocking Call
```kotlin
val data = fetchDataFromNetwork() // BAD - blocks main thread
```
### ✅ Suspend Function
```kotlin
viewModelScope.launch {
    val data = fetchDataFromNetwork() // GOOD
}
```

---

## 🎨 Quick Styling Reference

### Standard Padding
```kotlin
Modifier.padding(16.dp) // Standard padding
Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
```

### Standard Colors
```kotlin
PrimaryGreen // #0E8F5A - Main CTA
PrimaryGreenHover // #0C8050 - Hover state
TextPrimary // #0A0A0A - Primary text
TextSecondary // #4B5563 - Secondary text
```

### Standard Sizes
```kotlin
// Buttons
height = 48.dp // Primary button
height = 40.dp // Secondary button

// Touch targets
size = 48.dp // Minimum touch target

// Card
width = 380.dp
height = 470.dp
cornerRadius = 28.dp
```

---

**Remember:** When in doubt, check the [ANDROID_DEV_GUIDE.md](./ANDROID_DEV_GUIDE.md) for detailed explanations!
