# Banking Form State Persistence Implementation

## Overview
This implementation provides automatic form progress saving and resumption for the Banking module's multi-step form process. When users complete forms 1 and 2, the app automatically navigates to form 3 on the next launch.

## Architecture Components

### 1. BankingFormStateRepository
**Location:** `feature/banking/src/main/java/com/example/moviesdemoapp/feature/banking/data/BankingFormStateRepository.kt`

Manages persistence of form completion state using SharedPreferences.

**Key Methods:**
```kotlin
// Mark a form as completed
fun markFormCompleted(formNumber: Int, formData: String? = null)

// Check if a form is completed
fun isFormCompleted(formNumber: Int): Boolean

// Get the form to resume from
fun getFormToResume(): Int

// Check if forms 1 & 2 are complete (can proceed to form 3)
fun canProceedToForm3(): Boolean

// Reset all form state
fun resetAllForms()
```

### 2. Updated BankingPageState
**Location:** `feature/banking/src/main/java/com/example/moviesdemoapp/feature/banking/ui/model/BankingContract.kt`

**New State Fields:**
```kotlin
val currentFormNumber: Int = 1
val isForm1Completed: Boolean = false
val isForm2Completed: Boolean = false
val isForm3Completed: Boolean = false
val isForm4Completed: Boolean = false
```

### 3. New BankingPageIntent Events
```kotlin
data class MarkFormCompleted(val formNumber: Int, val formData: String? = null)
data object CheckAndNavigateToNextForm
data object ResumeFromSavedState
```

### 4. New BankingPageEffect Events
```kotlin
data class AutoNavigate(val formNumber: Int)
```

## Integration Steps

### Step 1: Update MainActivity Navigation Call

When setting up the banking navigation graph, pass the `BankingFormStateRepository`:

```kotlin
// In MainActivity or your navigation setup
val formStateRepository = LocalContext.current.get<BankingFormStateRepository>()

NavHost(navController = navController, startDestination = "banking_graph") {
    bankingGraph(
        navController = navController,
        formStateRepository = formStateRepository
    )
}
```

### Step 2: Dependency Injection (Hilt Module)

Ensure `BankingFormStateRepository` is provided via Hilt:

```kotlin
@Module
@InstallIn(SingletonComponent::class)
object BankingModule {
    
    @Provides
    @Singleton
    fun provideBankingFormStateRepository(
        @ApplicationContext context: Context
    ): BankingFormStateRepository {
        return BankingFormStateRepository(context)
    }
}
```

## Form Flow

### Form 1: Personal Details
- **Route:** `banking`
- **Completion Trigger:** When user taps "Continue" button
- **Action:** Marks form 1 as completed and saves data

### Form 2: Address Details
- **Route:** `banking_address`
- **Completion Trigger:** When user taps "Continue" button
- **Action:** Marks form 2 as completed and saves data

### Form 3: Financial Information
- **Route:** `banking_financial_detail`
- **Auto-Navigation:** If forms 1 & 2 are complete on app launch, automatically navigates here
- **Completion Trigger:** When user taps "Continue" button
- **Action:** Marks form 3 as completed and saves data

### Form 4: Review & Submit
- **Route:** `banking_review_submit`
- **Completion Trigger:** When user taps "Submit" button
- **Action:** Marks form 4 as complete and saves data

## Key Features

### 1. Automatic Progress Saving
```kotlin
// When form is submitted, this is called:
viewModel.handleIntent(
    BankingPageIntent.MarkFormCompleted(formNumber, formData)
)
```

### 2. Auto-Navigation to Form 3
```kotlin
// On app launch, checks if forms 1 & 2 are complete
if (completionStatus.canProceedToForm3() && lastNavigatedPage != Routes.BANKING_FINENCIAL_DETAIL) {
    navController.navigate(Routes.BANKING_FINENCIAL_DETAIL)
}
```

### 3. Form Resumption
```kotlin
// Get the form user should resume from
val formToResume = formStateRepository.getFormToResume()
// Returns 1, 2, 3, or 4 depending on completion status
```

### 4. Data Persistence
```kotlin
// Save form data along with completion state
formStateRepository.markFormCompleted(
    formNumber = 2,
    formData = Json.encodeToString(addressData)
)

// Retrieve saved form data
val savedData = formStateRepository.getFormData(2)
```

## SharedPreferences Keys

```
banking_form_state preferences:
├── form_1_personal_details_completed (Boolean)
├── form_2_address_completed (Boolean)
├── form_3_financial_completed (Boolean)
├── form_4_review_completed (Boolean)
├── last_completed_form (Long)
├── form_1_data (String - JSON)
├── form_2_data (String - JSON)
└── form_3_data (String - JSON)
```

## Usage Examples

### Mark Form as Complete on Submit
```kotlin
// In BankingScreen or BankingIncrementScreen
BankingScreen(navController, viewModel) { route ->
    // Form 1 submission
    viewModel.handleIntent(
        BankingPageIntent.MarkFormCompleted(1, route)
    )
}
```

### Check Form Status
```kotlin
val completionStatus = formStateRepository.getFormCompletionStatus()
if (completionStatus.canProceedToForm3()) {
    // Navigate to form 3 automatically
}
```

### Reset All Progress (e.g., for logout or account switch)
```kotlin
formStateRepository.resetAllForms()
```

### Resume from Saved State
```kotlin
// Call when returning to banking section
viewModel.handleIntent(BankingPageIntent.ResumeFromSavedState)
```

## Testing

### Scenario 1: Complete Forms 1 & 2, Relaunch App
1. Launch app → Shows Form 1 (Personal Details)
2. Fill Form 1 → Tap Continue
3. Form 2 (Address) appears
4. Fill Form 2 → Tap Continue
5. **Kill app and relaunch**
6. **Expected:** Form 3 (Financial Information) automatically displayed

### Scenario 2: Close Mid-Form 1
1. Launch app → Shows Form 1
2. Fill partial Form 1 → Close app
3. **Kill app and relaunch**
4. **Expected:** Form 1 appears again with empty state (form 1 not marked complete)

### Scenario 3: Complete All Forms
1. Complete Forms 1, 2, 3, 4 in sequence
2. Relaunch app
3. **Expected:** Behavior depends on use case (show success page or allow restart)

## Clear Form State Example

```kotlin
// If you need to start fresh
formStateRepository.resetAllForms()

// Or clear specific form
formStateRepository.clearFormData(2) // Clear address data only
```

## Error Handling

The implementation handles:
- **Missing SharedPreferences:** Defaults to `false` for all completion states
- **Missing Form Data:** Returns `null` when retrieving saved data
- **Concurrent Access:** Uses standard SharedPreferences thread-safety

## Performance Considerations

- **Storage:** Minimal (4 booleans + 3 optional JSON strings ≈ 1KB per user)
- **Read Time:** < 1ms (SharedPreferences)
- **Write Time:** < 5ms (async via `edit().apply()`)
- **No Network Calls:** Entirely local

## Future Enhancements

1. **Server Sync:** Upload form progress to backend for cross-device support
2. **Encryption:** Use EncryptedSharedPreferences for sensitive data
3. **Timeout:** Auto-reset forms if unchanged for X days
4. **Analytics:** Track form abandonment and completion rates
5. **Conditional Fields:** Show/hide fields based on previous form answers

