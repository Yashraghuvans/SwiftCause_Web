# Android Development Guide - SwiftCause

This document outlines the development standards, best practices, and architectural patterns for the SwiftCause Android application.

---

## Table of Contents

1. [Architecture](#architecture)
2. [Project Structure](#project-structure)
3. [Naming Conventions](#naming-conventions)
4. [Internationalization (i18n)](#internationalization-i18n)
5. [UI/UX Standards](#uiux-standards)
6. [State Management](#state-management)
7. [Networking](#networking)
8. [Data Persistence](#data-persistence)
9. [Security](#security)
10. [Testing](#testing)
11. [Code Style](#code-style)
12. [Git Workflow](#git-workflow)

---

## Architecture

### MVVM (Model-View-ViewModel) Pattern

We follow the **MVVM architecture** with Clean Architecture principles.

```
┌─────────────────────────────────────────────────────────┐
│                    Presentation Layer                   │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐  │
│  │   Screen     │◄─│  ViewModel   │◄─│   UseCase    │  │
│  │  (Compose)   │  │  (StateFlow) │  │   (Domain)   │  │
│  └──────────────┘  └──────────────┘  └──────────────┘  │
└─────────────────────────────────────────────────────────┘
                          ▲
                          │
┌─────────────────────────────────────────────────────────┐
│                     Domain Layer                        │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐  │
│  │   UseCase    │  │  Repository  │  │    Models    │  │
│  │              │  │  Interface   │  │   (Entities) │  │
│  └──────────────┘  └──────────────┘  └──────────────┘  │
└─────────────────────────────────────────────────────────┘
                          ▲
                          │
┌─────────────────────────────────────────────────────────┐
│                      Data Layer                         │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐  │
│  │  Repository  │  │   API/Remote │  │  Local/Cache │  │
│  │     Impl     │  │  DataSource  │  │  DataSource  │  │
│  └──────────────┘  └──────────────┘  └──────────────┘  │
└─────────────────────────────────────────────────────────┘
```

### Layer Responsibilities

#### Presentation Layer
- **Screens (Composables)**: UI components, no business logic
- **ViewModels**: Handle UI state, user interactions, coordinate use cases
- **UI State**: Sealed classes/data classes for screen states

#### Domain Layer
- **Use Cases**: Single responsibility business logic operations
- **Models**: Business entities (pure Kotlin classes)
- **Repository Interfaces**: Data operation contracts

#### Data Layer
- **Repository Implementations**: Concrete data operations
- **Data Sources**: API clients, database DAOs, DataStore
- **Mappers**: Convert between data models and domain models

---

## Project Structure

```
app/src/main/java/com/example/swiftcause/
├── data/
│   ├── api/                    # Retrofit API interfaces
│   │   └── CampaignApi.kt
│   ├── local/                  # Room database, DataStore
│   │   ├── dao/
│   │   └── database/
│   ├── models/                 # Data Transfer Objects (DTOs)
│   │   ├── CampaignDto.kt
│   │   └── Mappers.kt
│   └── repository/             # Repository implementations
│       └── CampaignRepositoryImpl.kt
├── domain/
│   ├── models/                 # Business entities
│   │   └── Campaign.kt
│   ├── repository/             # Repository interfaces
│   │   └── CampaignRepository.kt
│   └── usecase/                # Use cases
│       ├── GetCampaignsUseCase.kt
│       └── GetCampaignByIdUseCase.kt
├── presentation/
│   ├── screens/                # Composable screens
│   │   ├── CampaignListScreen.kt
│   │   └── CampaignDetailScreen.kt
│   ├── components/             # Reusable UI components
│   │   ├── CampaignCard.kt
│   │   └── LoadingIndicator.kt
│   └── viewmodels/             # ViewModels
│       └── CampaignListViewModel.kt
├── ui/
│   └── theme/                  # Material Theme configuration
│       ├── Color.kt
│       ├── Theme.kt
│       └── Type.kt
├── utils/                      # Utility classes
│   ├── Constants.kt
│   ├── Extensions.kt
│   └── NetworkUtils.kt
└── MainActivity.kt
```

---

## Naming Conventions

### Files & Classes

| Type | Convention | Example |
|------|-----------|---------|
| Activity | `*Activity` | `MainActivity` |
| Fragment | `*Fragment` | `CampaignListFragment` |
| Screen | `*Screen` | `CampaignListScreen` |
| ViewModel | `*ViewModel` | `CampaignListViewModel` |
| Repository | `*Repository` / `*RepositoryImpl` | `CampaignRepository` |
| UseCase | `*UseCase` or `Verb*` | `GetCampaignsUseCase` |
| API Interface | `*Api` | `CampaignApi` |
| DTO | `*Dto` or `*Response` | `CampaignDto` |
| Mapper | `*Mapper` or `*Extensions` | `CampaignMapper` |
| Component | `*Component` or descriptive | `CampaignCard` |

### Variables & Functions

```kotlin
// Composable functions: PascalCase
@Composable
fun CampaignCard(campaign: Campaign) { }

// Regular functions: camelCase
fun formatCurrency(amount: Long): String { }

// Variables: camelCase
val campaignList = listOf<Campaign>()
val isLoading = false

// Constants: UPPER_SNAKE_CASE
const val BASE_URL = "https://api.swiftcause.com"
const val DATABASE_NAME = "swiftcause_db"

// Private members: camelCase with underscore prefix (optional)
private val _uiState = MutableStateFlow<UiState>(UiState.Loading)
val uiState: StateFlow<UiState> = _uiState.asStateFlow()
```

### Resource Files

| Type | Convention | Example |
|------|-----------|---------|
| Layout | `snake_case` | `activity_main.xml` |
| Drawable | `snake_case` with prefix | `ic_launcher.png`, `bg_card.xml` |
| String | `snake_case` | `app_name`, `campaign_title` |
| Dimension | `snake_case` | `button_height`, `card_padding` |
| Color | `snake_case` | `primary_green`, `text_secondary` |

---

## Internationalization (i18n)

### ❌ DON'T: Hardcode Strings

```kotlin
// BAD - Hardcoded strings
Text(text = "Donate")
Text(text = "Campaign List")
Text(text = "Goal $1,000")
```

### ✅ DO: Use String Resources

```kotlin
// GOOD - String resources
Text(text = stringResource(R.string.donate))
Text(text = stringResource(R.string.campaign_list))
Text(text = stringResource(R.string.goal_amount, formattedAmount))
```

### String Resource Guidelines

#### 1. Define in `res/values/strings.xml`

```xml
<resources>
    <!-- App Name -->
    <string name="app_name">SwiftCause</string>
    
    <!-- Campaign List Screen -->
    <string name="campaign_list_title">Campaigns</string>
    <string name="campaign_list_empty">No campaigns available</string>
    <string name="campaign_list_error">Failed to load campaigns</string>
    
    <!-- Campaign Card -->
    <string name="donate">Donate</string>
    <string name="goal_amount">Goal %1$s</string>
    <string name="raised_amount">%1$s raised</string>
    <string name="progress_percentage">%1$d%%</string>
    
    <!-- Common -->
    <string name="loading">Loading…</string>
    <string name="retry">Retry</string>
    <string name="cancel">Cancel</string>
    <string name="confirm">Confirm</string>
    
    <!-- Error Messages -->
    <string name="error_network">Network error. Please check your connection.</string>
    <string name="error_generic">Something went wrong. Please try again.</string>
</resources>
```

#### 2. Plurals for Countable Items

```xml
<plurals name="donors_count">
    <item quantity="one">%d donor</item>
    <item quantity="other">%d donors</item>
</plurals>

<plurals name="days_remaining">
    <item quantity="one">%d day remaining</item>
    <item quantity="other">%d days remaining</item>
</plurals>
```

```kotlin
// Usage
val donorsText = pluralStringResource(
    R.plurals.donors_count,
    count = donorCount,
    donorCount
)
```

#### 3. Localization Files Structure

```
res/
├── values/              # Default (English)
│   ├── strings.xml
│   └── strings_errors.xml
├── values-es/           # Spanish
│   └── strings.xml
├── values-fr/           # French
│   └── strings.xml
└── values-ar/           # Arabic (RTL)
    └── strings.xml
```

#### 4. String Formatting

```xml
<!-- Format with arguments -->
<string name="welcome_message">Welcome, %1$s!</string>
<string name="campaign_progress">%1$s of %2$s raised</string>
<string name="donors_progress">%1$d out of %2$d donors</string>
```

```kotlin
// Usage
stringResource(R.string.welcome_message, userName)
stringResource(R.string.campaign_progress, raised, goal)
```

---

## UI/UX Standards

### Design System

#### Colors
- Use colors defined in `Color.kt`
- Follow Material 3 theming
- Support light/dark mode

```kotlin
// ✅ GOOD - Using theme colors
Text(
    text = "Donate",
    color = MaterialTheme.colorScheme.primary
)

// ✅ GOOD - Using brand colors
Text(
    text = "Donate",
    color = PrimaryGreen
)

// ❌ BAD - Hardcoded colors
Text(
    text = "Donate",
    color = Color(0xFF0E8F5A)
)
```

#### Dimensions
Define common dimensions in `Dimensions.kt`:

```kotlin
object Dimensions {
    val PaddingSmall = 8.dp
    val PaddingMedium = 16.dp
    val PaddingLarge = 24.dp
    
    val CardCornerRadius = 28.dp
    val ButtonCornerRadius = 24.dp
    
    val CampaignCardWidth = 380.dp
    val CampaignCardHeight = 470.dp
    val CampaignImageHeight = 245.dp
}
```

#### Typography
Use Material 3 typography:

```kotlin
Text(
    text = "Campaign Title",
    style = MaterialTheme.typography.titleLarge
)

Text(
    text = "Description",
    style = MaterialTheme.typography.bodyMedium
)
```

### Accessibility

#### 1. Content Descriptions
```kotlin
// ✅ GOOD - Content description for screen readers
Image(
    painter = painterResource(R.drawable.ic_heart),
    contentDescription = stringResource(R.string.favorite_icon)
)

// ❌ BAD - Missing content description
Image(
    painter = painterResource(R.drawable.ic_heart),
    contentDescription = null
)
```

#### 2. Minimum Touch Targets
- Buttons and clickable elements: **48dp minimum**
- Icons: **48dp touch target** (24dp icon + padding)

```kotlin
IconButton(
    onClick = { },
    modifier = Modifier.size(48.dp) // Touch target
) {
    Icon(
        imageVector = Icons.Default.Favorite,
        contentDescription = stringResource(R.string.favorite),
        modifier = Modifier.size(24.dp) // Icon size
    )
}
```

#### 3. Semantic Properties
```kotlin
Text(
    text = "Important message",
    modifier = Modifier.semantics {
        heading()
    }
)
```

---

## State Management

### ViewModel Pattern

#### UI State Classes

```kotlin
// Sealed class for screen states
sealed interface CampaignListUiState {
    data object Loading : CampaignListUiState
    data class Success(val campaigns: List<Campaign>) : CampaignListUiState
    data class Error(val message: String) : CampaignListUiState
}

// Data class for form states
data class DonationFormState(
    val amount: String = "",
    val isAmountValid: Boolean = false,
    val isProcessing: Boolean = false,
    val error: String? = null
)
```

#### ViewModel Implementation

```kotlin
class CampaignListViewModel(
    private val getCampaignsUseCase: GetCampaignsUseCase
) : ViewModel() {

    // Private mutable state
    private val _uiState = MutableStateFlow<CampaignListUiState>(
        CampaignListUiState.Loading
    )
    
    // Public immutable state
    val uiState: StateFlow<CampaignListUiState> = _uiState.asStateFlow()

    init {
        loadCampaigns()
    }

    fun loadCampaigns() {
        viewModelScope.launch {
            _uiState.value = CampaignListUiState.Loading
            
            getCampaignsUseCase()
                .onSuccess { campaigns ->
                    _uiState.value = CampaignListUiState.Success(campaigns)
                }
                .onFailure { error ->
                    _uiState.value = CampaignListUiState.Error(
                        error.message ?: "Unknown error"
                    )
                }
        }
    }

    fun onCampaignClick(campaignId: String) {
        // Handle campaign click
    }
}
```

#### Screen Implementation

```kotlin
@Composable
fun CampaignListScreen(
    viewModel: CampaignListViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    when (val state = uiState) {
        is CampaignListUiState.Loading -> {
            LoadingScreen()
        }
        is CampaignListUiState.Success -> {
            CampaignGrid(
                campaigns = state.campaigns,
                onCampaignClick = viewModel::onCampaignClick
            )
        }
        is CampaignListUiState.Error -> {
            ErrorScreen(
                message = state.message,
                onRetry = viewModel::loadCampaigns
            )
        }
    }
}
```

---

## Networking

### Retrofit Setup

#### API Interface
```kotlin
interface CampaignApi {
    @GET("campaigns")
    suspend fun getCampaigns(
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 20
    ): Response<CampaignsResponse>

    @GET("campaigns/{id}")
    suspend fun getCampaignById(
        @Path("id") campaignId: String
    ): Response<CampaignDto>
}
```

#### Repository Implementation
```kotlin
class CampaignRepositoryImpl(
    private val api: CampaignApi
) : CampaignRepository {

    override suspend fun getCampaigns(): Result<List<Campaign>> {
        return try {
            val response = api.getCampaigns()
            if (response.isSuccessful) {
                val campaigns = response.body()?.data?.map { it.toDomain() }
                Result.success(campaigns ?: emptyList())
            } else {
                Result.failure(Exception("Error: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
```

### Error Handling

```kotlin
sealed class NetworkError : Exception() {
    data class HttpError(val code: Int, override val message: String) : NetworkError()
    data class NetworkException(override val message: String) : NetworkError()
    data object Timeout : NetworkError()
    data object NoInternet : NetworkError()
}

suspend fun <T> safeApiCall(
    apiCall: suspend () -> Response<T>
): Result<T> {
    return try {
        val response = apiCall()
        if (response.isSuccessful) {
            Result.success(response.body()!!)
        } else {
            Result.failure(
                NetworkError.HttpError(response.code(), response.message())
            )
        }
    } catch (e: IOException) {
        Result.failure(NetworkError.NoInternet)
    } catch (e: Exception) {
        Result.failure(NetworkError.NetworkException(e.message ?: "Unknown"))
    }
}
```

---

## Data Persistence

### DataStore (Preferences)

```kotlin
class UserPreferencesRepository(
    private val dataStore: DataStore<Preferences>
) {
    companion object {
        private val KIOSK_SESSION_KEY = stringPreferencesKey("kiosk_session")
        private val THEME_MODE_KEY = stringPreferencesKey("theme_mode")
    }

    val kioskSession: Flow<String?> = dataStore.data
        .map { preferences -> preferences[KIOSK_SESSION_KEY] }

    suspend fun saveKioskSession(session: String) {
        dataStore.edit { preferences ->
            preferences[KIOSK_SESSION_KEY] = session
        }
    }

    suspend fun clearKioskSession() {
        dataStore.edit { preferences ->
            preferences.remove(KIOSK_SESSION_KEY)
        }
    }
}
```

### Room Database (Future)

```kotlin
@Entity(tableName = "campaigns")
data class CampaignEntity(
    @PrimaryKey val id: String,
    val title: String,
    val coverImageUrl: String?,
    val raised: Long,
    val goal: Long,
    val currency: String,
    @ColumnInfo(name = "created_at") val createdAt: Long
)

@Dao
interface CampaignDao {
    @Query("SELECT * FROM campaigns")
    fun getAllCampaigns(): Flow<List<CampaignEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCampaigns(campaigns: List<CampaignEntity>)

    @Query("DELETE FROM campaigns")
    suspend fun clearAll()
}
```

---

## Security

### 1. API Keys & Secrets
- **Never commit API keys** to version control
- Store in `local.properties` (gitignored)
- Access via BuildConfig

```gradle
// build.gradle.kts
android {
    defaultConfig {
        buildConfigField("String", "API_KEY", "\"${project.property("API_KEY")}\"")
    }
}
```

```kotlin
// Usage
val apiKey = BuildConfig.API_KEY
```

### 2. Network Security
- Use HTTPS only
- Implement certificate pinning for production

```xml
<!-- res/xml/network_security_config.xml -->
<network-security-config>
    <domain-config>
        <domain includeSubdomains="true">api.swiftcause.com</domain>
        <pin-set>
            <pin digest="SHA-256">base64encodedpin</pin>
        </pin-set>
    </domain-config>
</network-security-config>
```

### 3. Data Encryption
- Use EncryptedSharedPreferences for sensitive data
- Encrypt database if storing payment info

---

## Testing

### Unit Tests

```kotlin
class GetCampaignsUseCaseTest {
    
    private lateinit var repository: FakeCampaignRepository
    private lateinit var useCase: GetCampaignsUseCase

    @Before
    fun setup() {
        repository = FakeCampaignRepository()
        useCase = GetCampaignsUseCase(repository)
    }

    @Test
    fun `getCampaigns returns success when repository succeeds`() = runTest {
        // Given
        val campaigns = listOf(
            Campaign(id = "1", title = "Test", goal = 1000)
        )
        repository.setSuccessResponse(campaigns)

        // When
        val result = useCase()

        // Then
        assertTrue(result.isSuccess)
        assertEquals(campaigns, result.getOrNull())
    }
}
```

### UI Tests (Compose)

```kotlin
class CampaignCardTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun campaignCard_displaysCorrectInfo() {
        // Given
        val campaign = Campaign(
            id = "1",
            title = "Test Campaign",
            raised = 45000,
            goal = 1000
        )

        // When
        composeTestRule.setContent {
            CampaignCard(campaign = campaign)
        }

        // Then
        composeTestRule.onNodeWithText("Test Campaign").assertExists()
        composeTestRule.onNodeWithText("$450").assertExists()
        composeTestRule.onNodeWithText("Goal $1,000").assertExists()
    }
}
```

---

## Code Style

### Kotlin Coding Conventions

Follow [Kotlin Coding Conventions](https://kotlinlang.org/docs/coding-conventions.html):

```kotlin
// ✅ GOOD - Proper formatting
class CampaignViewModel(
    private val getCampaignsUseCase: GetCampaignsUseCase,
    private val saveCampaignUseCase: SaveCampaignUseCase
) : ViewModel() {

    fun loadCampaigns() {
        viewModelScope.launch {
            // Implementation
        }
    }
}

// ❌ BAD - Poor formatting
class CampaignViewModel(private val getCampaignsUseCase: GetCampaignsUseCase,private val saveCampaignUseCase: SaveCampaignUseCase):ViewModel(){
    fun loadCampaigns(){viewModelScope.launch{}}
}
```

### Compose Best Practices

```kotlin
// ✅ GOOD - Stateless composable
@Composable
fun CampaignCard(
    campaign: Campaign,
    onCardClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    // UI implementation
}

// ✅ GOOD - Stateful composable with hoisting
@Composable
fun CampaignListScreen(
    viewModel: CampaignListViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    
    CampaignListContent(
        uiState = uiState,
        onRefresh = viewModel::loadCampaigns
    )
}

@Composable
private fun CampaignListContent(
    uiState: CampaignListUiState,
    onRefresh: () -> Unit
) {
    // UI implementation
}
```

---

## Git Workflow

### Branch Naming

| Type | Convention | Example |
|------|-----------|---------|
| Feature | `feature/<description>` | `feature/campaign-detail-screen` |
| Bug Fix | `fix/<description>` | `fix/crash-on-launch` |
| Hotfix | `hotfix/<description>` | `hotfix/payment-validation` |
| Refactor | `refactor/<description>` | `refactor/viewmodel-structure` |

### Commit Messages

Follow [Conventional Commits](https://www.conventionalcommits.org/):

```
<type>: <description>

[optional body]

[optional footer]
```

Types:
- `feat`: New feature
- `fix`: Bug fix
- `refactor`: Code refactoring
- `style`: Formatting, missing semicolons, etc.
- `test`: Adding tests
- `docs`: Documentation
- `chore`: Maintenance

Examples:
```
feat: add campaign detail screen with donation form

fix: resolve crash when loading campaigns without internet

refactor: extract currency formatting to utility class

chore: update dependencies to latest versions
```

---

## Performance Optimization

### 1. Lazy Loading
```kotlin
// ✅ GOOD - Lazy loading with LazyColumn/LazyGrid
LazyVerticalGrid(
    columns = GridCells.Adaptive(minSize = 380.dp)
) {
    items(campaigns) { campaign ->
        CampaignCard(campaign = campaign)
    }
}
```

### 2. Image Loading
```kotlin
// ✅ GOOD - Proper image loading with Coil
AsyncImage(
    model = ImageRequest.Builder(LocalContext.current)
        .data(campaign.coverImageUrl)
        .crossfade(true)
        .size(Size.ORIGINAL)
        .build(),
    contentDescription = campaign.title,
    contentScale = ContentScale.Crop,
    placeholder = painterResource(R.drawable.placeholder_campaign),
    error = painterResource(R.drawable.error_campaign)
)
```

### 3. Remember & Derivations
```kotlin
// ✅ GOOD - Remember expensive calculations
@Composable
fun CampaignStats(campaigns: List<Campaign>) {
    val totalRaised = remember(campaigns) {
        campaigns.sumOf { it.raised }
    }
    
    Text("Total raised: ${formatCurrency(totalRaised)}")
}
```

---

## Documentation

### KDoc Comments

```kotlin
/**
 * Fetches a list of active campaigns from the API.
 *
 * @param page The page number for pagination (default: 1)
 * @param limit The number of campaigns per page (default: 20)
 * @return Result containing list of campaigns or error
 *
 * @throws NetworkException if network request fails
 */
suspend fun getCampaigns(
    page: Int = 1,
    limit: Int = 20
): Result<List<Campaign>>
```

---

## Common Pitfalls to Avoid

### ❌ DON'T:
1. Hardcode strings - Use `strings.xml`
2. Perform network calls in composables - Use ViewModels
3. Store state in composables - Hoist to ViewModels
4. Ignore error handling - Always handle failures
5. Commit API keys - Use BuildConfig
6. Block the main thread - Use coroutines
7. Ignore accessibility - Add content descriptions
8. Hardcode colors - Use theme colors

### ✅ DO:
1. Use string resources for all UI text
2. Follow MVVM architecture
3. Write unit tests for business logic
4. Handle all error cases gracefully
5. Support dark mode
6. Use Material 3 design system
7. Implement proper loading states
8. Add accessibility features

---

## Tools & Libraries

### Required
- **Jetpack Compose** - UI toolkit
- **Kotlin Coroutines** - Asynchronous programming
- **StateFlow/SharedFlow** - State management
- **ViewModel** - UI state holder
- **Retrofit** - HTTP client
- **Coil** - Image loading
- **DataStore** - Preferences storage
- **Firebase** - Authentication, Firestore

### Recommended
- **Room** - Local database (future)
- **Hilt** - Dependency injection (future)
- **Timber** - Logging
- **LeakCanary** - Memory leak detection

---

## Questions or Suggestions?

If you have questions about these guidelines or suggestions for improvements, please:
1. Create an issue in the repository
2. Discuss in team meetings
3. Update this document with team consensus

**Last Updated:** 2026-03-27
**Version:** 1.0.0
