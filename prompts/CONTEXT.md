# MovieApp — Session Context
# Load this file in EVERY claude session without exception.

## Project
Name: MovieApp
Package: com.example.moviesdemoapp
Platform: Android (Kotlin + Jetpack Compose)
Assignment: Production-grade Server-Driven UI movie + banking app

## Stack
- Language: Kotlin 2.0+
- UI: Jetpack Compose + Material3 (no XML layouts)
- Architecture: MVI (strict unidirectional data flow)
- DI: Hilt
- Network: Retrofit 2 + OkHttp (live OMDb API + MockInterceptor for banking)
- DB: Room 2.6+
- Async: Coroutines + StateFlow + Channel
- Navigation: Compose Navigation (API-driven)
- Image: Coil 3.x
- Serialization: Kotlinx Serialization
- Testing: JUnit 4 + MockK + Turbine

## Module Map
:app                  → entry point, NavHost, SplashActivity, Hilt app
:core:ui              → design system, tokens, base components
:core:network         → Retrofit, OkHttp, interceptors, base models
:core:data            → Room, base repository, DAOs
:core:domain          → BaseViewModel, BaseUseCase, Result wrapper, UiState/UiIntent/UiEffect
:engine:sdui          → SDUIRenderer, ComponentRegistry, ActionHandler, SDUIParser
:engine:navigation    → NavigationEngine, NavigationAction, Routes
:feature:movies       → TV series list + series detail (OMDb live API, SDUI-driven)
:feature:banking      → Banking placeholder tab (SDUI-driven, mocked)

## App Entry
- SplashScreen → MainActivity → ArchitectNavHost
- MainScreen: BottomNav with 2 tabs
  - Tab 1: Movies (icon: movie, label: Movies)
  - Tab 2: Banking (icon: account_balance, label: Banking)

## Absolute Rules (never violate)
1. NO hardcoded UI — every screen rendered via SDUIRenderer from JSON
2. NO hardcoded navigation — all routes from NavigationAction
3. MVI only — State/Intent/Effect per feature, BaseViewModel in :core:domain
4. Hilt for ALL dependency injection
5. Never modify :core or :engine files unless explicitly instructed
6. All values from design tokens — no magic colors/sizes/strings
7. Every public API must have KDoc
8. Feature modules must NOT depend on each other
9. SOLID principles and clean architecture in every file
10. Room for local persistence, Coil for images, JUnit+MockK for tests
