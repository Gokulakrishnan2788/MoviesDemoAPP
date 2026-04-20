# Navigation Contract

## NavHost lives ONLY in :app (ArchitectNavHost.kt)
## Features expose NavGraphBuilder extension functions

## Full Navigation Graph
RootNavHost (startDestination = SPLASH)
│
├── SPLASH ─────────────────────────► MAIN (replace, back stack cleared)
│
└── MAIN
    └── MainScreen (BottomNav — 2 tabs)
        ├── TAB_MOVIES   → MoviesNavHost (nested)
        │     ├── MOVIES               → MoviesScreen (series list)
        │     └── SERIES_DETAIL/{id}   → SeriesDetailScreen
        │
        └── TAB_BANKING  → BankingScreen

## Bottom Nav Tabs
Tab 1: Movies
  - icon: Icons.Default.Movie (or movie icon)
  - label: "Movies"
  - route: Routes.MOVIES

Tab 2: Banking
  - icon: Icons.Default.AccountBalance
  - label: "Banking"
  - route: Routes.BANKING

## Navigation Actions
SERIES_TAPPED:
  type: PUSH
  destination: Routes.seriesDetail(imdbID)
  params: { "seriesId": imdbID }

BACK:
  type: POP

## NavigationEngine
object NavigationEngine {
    fun navigate(navController: NavController, action: NavigationAction) {
        when (action.type) {
            NavType.PUSH     → navController.navigate(action.destination)
            NavType.REPLACE  → navController.navigate(action.destination) { popUpTo(0) { inclusive = true } }
            NavType.POP      → navController.popBackStack()
            NavType.DEEP_LINK→ navController.navigate(deepLinkUri)
        }
    }
}

## Feature NavGraph Extensions
// In :feature:movies
fun NavGraphBuilder.moviesGraph(navController: NavController) {
    navigation(startDestination = Routes.MOVIES, route = "movies_graph") {
        composable(Routes.MOVIES) { MoviesScreen(navController) }
        composable(Routes.SERIES_DETAIL) { backStackEntry ->
            val seriesId = backStackEntry.arguments?.getString("seriesId") ?: ""
            SeriesDetailScreen(navController, seriesId)
        }
    }
}

// In :feature:banking
fun NavGraphBuilder.bankingGraph(navController: NavController) {
    composable(Routes.BANKING) { BankingScreen(navController) }
}
