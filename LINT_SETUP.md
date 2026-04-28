# Lint & Code-Style Setup

This document describes the static-analysis and code-style tooling configured for every module in **MoviesDemoAPP**.  
Follow these instructions when onboarding, adding a new module, or triaging a gate failure.

---

## Table of Contents

1. [Tools Overview](#1-tools-overview)
2. [KTLint — Kotlin Code Style](#2-ktlint--kotlin-code-style)
3. [Android Lint — Static Analysis](#3-android-lint--static-analysis)
4. [Lint Baseline](#4-lint-baseline)
5. [Git Hooks](#5-git-hooks)
6. [Adding a New Module](#6-adding-a-new-module)
7. [Suppressing a Violation](#7-suppressing-a-violation)
8. [CI Integration](#8-ci-integration)
9. [Troubleshooting](#9-troubleshooting)

---

## 1. Tools Overview

| Tool | Purpose | Config file | Runs on |
|---|---|---|---|
| **KTLint** (v1.3.1) | Kotlin formatting & style | `.editorconfig` | Every commit (pre-commit hook) |
| **Android Lint** | Correctness, security, performance | `lint.xml` + `lint-baseline.xml` | Every push (pre-push hook) |

Both tools are enforced automatically via Git hooks (see §5) and can be run manually via Gradle tasks (see §2 and §3).

---

## 2. KTLint — Kotlin Code Style

### Plugin

KTLint is applied to **all submodules** from the root `build.gradle.kts` via the `subprojects {}` block:

```kotlin
// build.gradle.kts (root)
subprojects {
    apply(plugin = "org.jlleitschuh.gradle.ktlint")
    configure<org.jlleitschuh.gradle.ktlint.KtlintExtension> {
        version.set("1.3.1")
        android.set(true)
        outputToConsole.set(true)
        ignoreFailures.set(false)
    }
}
```

Plugin declaration is in `gradle/libs.versions.toml`:

```toml
[versions]
ktlint = "12.1.2"

[plugins]
ktlint = { id = "org.jlleitschuh.gradle.ktlint", version.ref = "ktlint" }
```

### Style rules

All KTLint rules are controlled via `.editorconfig` at the project root:

```ini
[*.{kt,kts}]
max_line_length = 120
ktlint_standard_no-wildcard-imports = enabled
ktlint_standard_function-naming = disabled   # Composable functions use PascalCase
ktlint_standard_trailing-comma-on-call-site = enabled
ktlint_standard_trailing-comma-on-declaration-site = enabled
ktlint_compose_composable-naming = enabled
```

### Gradle tasks

| Task | Effect |
|---|---|
| `./gradlew ktlintCheck` | Report all violations (fails the build if any) |
| `./gradlew ktlintFormat` | Auto-fix all correctable violations |
| `./gradlew :module:ktlintCheck` | Check a single module |
| `./gradlew :module:ktlintFormat` | Format a single module |

**Recommended workflow:** run `./gradlew ktlintFormat` before committing, then `./gradlew ktlintCheck` to confirm zero errors.

---

## 3. Android Lint — Static Analysis

### Configuration per module

Every module's `build.gradle.kts` contains:

```kotlin
android {
    lint {
        abortOnError      = true
        warningsAsErrors  = false
        htmlReport        = true
        htmlOutput        = file("${buildDir}/reports/lint-results-debug.html")
        lintConfig        = file("${rootDir}/lint.xml")
        baseline          = file("${rootDir}/lint-baseline.xml")
    }
}
```

`abortOnError = true` means any new **error**-severity violation that is not in the baseline will fail the build.

### Severity overrides

`lint.xml` at the project root controls global severity rules:

```xml
<lint>
    <!-- Hardcoded strings must use string resources -->
    <issue id="HardcodedText" severity="error" />

    <!-- App Indexing not used — suppress globally -->
    <issue id="GoogleAppIndexingWarning" severity="ignore" />

    <!-- Firebase declares WAKE_LOCK in its manifest — false positive in DI module -->
    <issue id="MissingPermission">
        <ignore path="**/analytics/di/AnalyticsModule.kt" />
    </issue>
</lint>
```

### Gradle tasks

| Task | Effect |
|---|---|
| `./gradlew lintDebug` | Run lint on all modules (debug variant) |
| `./gradlew :app:lintDebug` | Run lint on `:app` only |
| `./gradlew :app:updateLintBaseline` | Regenerate the baseline for `:app` |

HTML reports are written to `<module>/build/reports/lint-results-debug.html`.

---

## 4. Lint Baseline

`lint-baseline.xml` at the project root records **pre-existing** lint violations that were present when the tooling was first introduced. These are suppressed in all subsequent runs so the team can adopt lint incrementally — only newly introduced violations will fail the build.

### Updating the baseline

When you intentionally accept new pre-existing violations (e.g., after adding a third-party library that introduces warnings):

```bash
./gradlew :app:updateLintBaseline
```

> **Never add errors to the baseline to silence failures — fix the error instead.**  
> The baseline is for pre-existing warnings you cannot fix immediately, not a workaround for correctness violations.

### Stale baseline entries

If lint reports `"N errors/warnings were listed in the baseline file but not found in the project"`, those issues have been fixed. Run `updateLintBaseline` to clean up the snapshot.

---

## 5. Git Hooks

Two hooks are installed automatically into `.git/hooks/` the first time any Android module is built (via Gradle's `preBuild` dependency).

### `pre-commit` — runs KTLint check before every commit

```sh
#!/bin/sh
./gradlew ktlintCheck --daemon -q
# exits 1 and blocks the commit if violations are found
```

**What to do when it fails:**

```bash
./gradlew ktlintFormat   # auto-fix all correctable violations
git add -p               # review and re-stage
git commit               # retry
```

### `pre-push` — runs Android Lint before every push

```sh
#!/bin/sh
./gradlew lintDebug --daemon -q
# exits 1 and blocks the push if new errors are found
```

**What to do when it fails:**

1. Open `app/build/reports/lint-results-debug.html` for the full report.
2. Fix the error, or suppress it via `lint.xml` / `@SuppressLint` with a clear justification comment.
3. Re-push.

### Manual installation

If hooks are missing (e.g., after a fresh clone before a first build):

```bash
./gradlew installGitHooks
```

Hook source files live in `scripts/` and are committed to the repository so the whole team shares the same gates.

---

## 6. Adding a New Module

When you add a new Gradle module:

1. **KTLint** — no action needed; the root `subprojects {}` block applies it automatically.

2. **Android Lint** — add a `lint {}` block inside `android {}` in the new module's `build.gradle.kts`:

```kotlin
android {
    lint {
        abortOnError     = true
        warningsAsErrors = false
        htmlReport       = true
        htmlOutput       = file("${buildDir}/reports/lint-results-debug.html")
        lintConfig       = file("${rootDir}/lint.xml")
        baseline         = file("${rootDir}/lint-baseline.xml")
    }
}
```

3. Run `./gradlew :new-module:lintDebug` to confirm no new errors.

---

## 7. Suppressing a Violation

Suppressions should be rare and always documented.

### Option A — inline (smallest scope, preferred for one-off cases)

```kotlin
@SuppressLint("IssueId")  // reason: <explain why this is acceptable>
fun myFunction() { ... }
```

### Option B — `lint.xml` (preferred for third-party false positives)

```xml
<issue id="IssueId">
    <ignore path="**/path/to/File.kt" />
</issue>
```

### Option C — baseline (only for pre-existing violations during tooling adoption)

```bash
./gradlew :app:updateLintBaseline
```

> Always prefer Option A or B over the baseline. The baseline is not a suppression tool; it is a starting-line snapshot.

---

## 8. CI Integration

The hooks enforce quality locally. In CI, run the same commands in sequence:

```yaml
# Example GitHub Actions step
- name: KTLint check
  run: ./gradlew ktlintCheck --daemon

- name: Android Lint
  run: ./gradlew lintDebug --daemon

- name: Upload lint report
  if: failure()
  uses: actions/upload-artifact@v4
  with:
    name: lint-report
    path: "**/build/reports/lint-results-debug.html"
```

Both tasks respect the same `lint.xml`, `.editorconfig`, and baseline that developers use locally, so CI results are reproducible.

---

## 9. Troubleshooting

| Symptom | Likely cause | Fix |
|---|---|---|
| `ktlintCheck` fails after pulling | New style rule added | Run `./gradlew ktlintFormat` |
| `ktlintCheck` fails on Composable function names | `function-naming` rule misconfigured | Ensure `.editorconfig` has `ktlint_standard_function-naming = disabled` |
| `lintDebug` fails with `MissingPermission` on Firebase | False positive — Firebase declares the permission in its manifest | Add path ignore to `lint.xml` under `MissingPermission` |
| `lintDebug` fails with `HardcodedText` | A string was added directly in a layout/Composable | Move string to `strings.xml` |
| Hooks not firing | `.git/hooks/` not populated | Run `./gradlew installGitHooks` |
| Baseline has stale entries | Issues were fixed | Run `./gradlew :app:updateLintBaseline` |
| `BUILD FAILED` with `abortOnError` | New error-severity violation introduced | Fix the violation; do not suppress errors into the baseline |
