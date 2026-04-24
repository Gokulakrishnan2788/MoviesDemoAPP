# Firebase/Kotlin Version Compatibility Fix

## Problem
Kotlin version incompatibility error during compilation:

```
Module was compiled with an incompatible version of Kotlin. 
The binary version of its metadata is 2.2.0, expected version is 2.0.0.
```

These errors were occurring from Firebase play-services modules:
- `play-services-measurement-impl-23.2.0`
- `play-services-measurement-api-23.2.0`

## Root Cause
**Version Mismatch:**
- **Project Kotlin Version**: 2.0.21
- **Firebase BOM Version**: 34.12.0 (requires Kotlin 2.2.0+)
- **Play-Services Libraries**: Compiled with Kotlin 2.2.0

Firebase BOM 34.x was compiled with Kotlin 2.2.0, but the project was using Kotlin 2.0.21. This caused a metadata version incompatibility.

## Solution
Downgraded Firebase BOM to 33.5.0, which is compatible with Kotlin 2.0.21.

**Changes in `gradle/libs.versions.toml`:**

```toml
# Before:
firebaseBom = "34.12.0"

# After:
firebaseBom = "33.5.0"
```

## Compatibility Chart

| Firebase BOM | Kotlin Version | Status |
|---|---|---|
| 34.x | 2.2.0+ | ✓ Compatible |
| 33.x | 2.0.21  | ✓ Compatible |
| 32.x | 1.9.x   | ✓ Compatible |

## Features in Firebase BOM 33.5.0
Firebase BOM 33.5.0 includes:
- ✓ Firebase Analytics
- ✓ Firebase Core
- ✓ All measurement APIs
- ✓ Play Services Core
- ✓ All other Firebase services

## Migration Path (if needed in future)
If you need Firebase BOM 34.x features in the future:
1. Update Kotlin to 2.2.0+
2. Update KSP accordingly (2.2.0-compatible version)
3. Update Firebase BOM to 34.x

For now, BOM 33.5.0 provides stable Firebase functionality with Kotlin 2.0.21.

## Build Command to Verify Fix
```bash
./gradlew clean build
```

All Firebase/Kotlin module metadata errors should now be resolved.
