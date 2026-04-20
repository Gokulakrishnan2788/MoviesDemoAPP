# Banking Feature — Full Feature Prompt
# Module: :feature:banking
# This is a placeholder SDUI-driven banking tab

## Files to generate:
# feature/banking/ui/BankingState.kt
# feature/banking/ui/BankingViewModel.kt
# feature/banking/ui/BankingScreen.kt
# feature/banking/di/BankingModule.kt
# assets/screens/banking_home.json   ← mock SDUI JSON for banking tab

## banking_home.json content:
{
  "screenId": "banking_home",
  "type": "scroll",
  "children": [
    {
      "id": "bankingColumn",
      "type": "column",
      "style": { "padding": 20, "spacing": 24, "backgroundColor": "screenBackground" },
      "children": [
        {
          "id": "bankingHeader",
          "type": "header",
          "titleTemplate": "Banking",
          "subtitleTemplate": "Your financial overview",
          "style": { "spacing": 6, "foregroundColor": "primaryText" }
        },
        {
          "id": "balanceCard",
          "type": "column",
          "style": { "padding": 20, "spacing": 8, "backgroundColor": "cardBackground", "cornerRadius": 24 },
          "children": [
            { "id": "balanceLabel", "type": "text", "text": "Total Balance",
              "style": { "fontSize": 14, "foregroundColor": "secondaryText" } },
            { "id": "balanceAmount", "type": "text", "text": "$12,450.00",
              "style": { "fontSize": 32, "fontWeight": "bold", "foregroundColor": "primaryText" } }
          ]
        },
        {
          "id": "comingSoonCard",
          "type": "column",
          "style": { "padding": 20, "spacing": 12, "backgroundColor": "surface", "cornerRadius": 22 },
          "children": [
            { "id": "comingSoonTitle", "type": "text", "text": "More features coming soon",
              "style": { "fontSize": 18, "fontWeight": "bold", "foregroundColor": "primaryText" } },
            { "id": "comingSoonSubtitle", "type": "text",
              "text": "Transfers, payments, account management and more.",
              "style": { "fontSize": 15, "foregroundColor": "secondaryText" } }
          ]
        }
      ]
    }
  ]
}

## BankingState / BankingIntent / BankingEffect
data class BankingState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val screenModel: ScreenModel? = null
) : UiState

sealed class BankingIntent : UiIntent {
    object LoadScreen : BankingIntent()
    data class HandleAction(val actionId: String) : BankingIntent()
}

sealed class BankingEffect : UiEffect {
    data class ShowToast(val message: String) : BankingEffect()
}

## BankingViewModel flow:
1. LoadScreen intent → load assets/screens/banking_home.json
2. Parse with SDUIParser → setState { copy(screenModel = ...) }
3. No live data fetch needed (static SDUI for now)

## BankingScreen
- Uses SDUIRenderer with static screenModel (no dataMap needed)
- Simple SDUI render — no live API
