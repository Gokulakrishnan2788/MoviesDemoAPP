# Dynamic Bottom Navigation Configuration - Implementation Guide

## Overview
The bottom navigation bar has been converted from hardcoded configuration to dynamic JSON-based configuration, allowing you to easily modify navigation items without changing code.

## Files Created/Modified

### 1. **bottom_nav_config.json** (New)
**Location:** `app/src/main/assets/bottom_nav_config.json`

This JSON file defines all bottom navigation items dynamically:

```json
{
  "bottomNavItems": [
    {
      "route": "movies_graph",
      "label": "Movies",
      "icon": "movie"
    },
    {
      "route": "banking_graph",
      "label": "Banking",
      "icon": "account_balance"
    }
  ]
}
```

**Fields:**
- `route`: Navigation graph route to navigate to when item is clicked
- `label`: Display label for the navigation item
- `icon`: Icon name (mapped in IconMapper.kt)

### 2. **BottomNavConfig.kt** (New)
**Location:** `app/src/main/java/com/example/moviesdemoapp/app/BottomNavConfig.kt`

Data classes that represent the JSON structure:
- `BottomNavItemConfig`: Single navigation item configuration
- `BottomNavConfig`: Container for all navigation items

### 3. **BottomNavConfigLoader.kt** (New)
**Location:** `app/src/main/java/com/example/moviesdemoapp/app/BottomNavConfigLoader.kt`

Utility object that:
- Loads the JSON file from assets
- Parses it using kotlinx.serialization
- Handles errors gracefully with fallback empty configuration

### 4. **IconMapper.kt** (New)
**Location:** `app/src/main/java/com/example/moviesdemoapp/app/IconMapper.kt`

Maps icon names from JSON strings to Material3 Icons:
- `"movie"` → `Icons.Default.Movie`
- `"account_balance"` → `Icons.Default.AccountBalance`

Can be extended to support more icons by adding new mappings.

### 5. **MainScreen.kt** (Modified)
**Location:** `app/src/main/java/com/example/moviesdemoapp/app/MainScreen.kt`

Changes:
- Removed hardcoded icon imports
- Added `LocalContext` import to access Android Context
- Added `remember` import for memoization
- Updated `MainScreen` composable to:
  - Load context using `LocalContext.current`
  - Use `remember` to load configuration once
  - Load navigation items from JSON using `BottomNavConfigLoader`
  - Convert configuration items to `BottomNavItem` using `IconMapper`

## How It Works

1. **Configuration Loading**: When `MainScreen` composable is created, it loads the JSON configuration from assets once and caches it using `remember`.

2. **Icon Mapping**: Icon names from JSON are converted to actual Material3 `ImageVector` objects using `IconMapper`.

3. **Dynamic Rendering**: The bottom navigation bar renders items based on the loaded configuration.

## How to Add New Navigation Items

Edit `app/src/main/assets/bottom_nav_config.json` and add a new item:

```json
{
  "route": "new_feature_graph",
  "label": "New Feature",
  "icon": "settings"  // Make sure to add mapping in IconMapper.kt
}
```

Then add the icon mapping in `IconMapper.kt`:

```kotlin
"settings" -> Icons.Default.Settings
```

## How to Add New Icon Mappings

Edit `IconMapper.kt` and add the mapping in the `when` expression:

```kotlin
object IconMapper {
    fun getIcon(iconName: String): ImageVector {
        return when (iconName.lowercase()) {
            "movie" -> Icons.Default.Movie
            "account_balance" -> Icons.Default.AccountBalance
            "settings" -> Icons.Default.Settings  // Add here
            else -> Icons.Default.Movie // Default icon fallback
        }
    }
}
```

## Benefits

✅ **No Code Changes Required**: Add or remove navigation items by editing JSON only
✅ **Type-Safe**: Uses Kotlin data classes with serialization
✅ **Error Handling**: Gracefully handles JSON parsing errors
✅ **Performance**: Configuration is loaded once and cached
✅ **Extensible**: Easy to add more icons or configuration options

## Error Handling

If the JSON file cannot be loaded or parsed, the loader returns an empty configuration as a fallback. This prevents the app from crashing while allowing you to debug the issue.

## Dependencies

The implementation uses:
- **kotlinx.serialization**: Already in your project (via `libs.plugins.kotlin.serialization`)
- **Android Context**: Standard Android framework
- **Compose**: Already in your project

