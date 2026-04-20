# Architecture Reference
# Load only for: initial setup, new module creation, core/engine changes

## Layer Order (strict — no skipping)
Compose UI → ViewModel (MVI) → UseCase → Repository → ApiService / DAO

## Module Dependency Rules
:feature:*  → can use :core:*, :engine:*
:engine:*   → can use :core:*
:core:*     → no internal cross-dependencies
:app        → can use everything (wiring only)

## SDUI Engine Architecture
JSON string (from assets OR live API)
    ↓
SDUIParser (Kotlinx Serialization) → ScreenModel
    ↓
SDUIRenderer → ComponentRegistry.resolve(type) → @Composable
    ↓
ActionHandler → NavigationEngine or ViewModel Intent

## SDUI JSON Structure
Each screen JSON has:
{
  "screenId": "string",
  "dataSource": {              ← optional: live API to fetch data after render
    "type": "remote",
    "method": "GET",
    "url": "https://...",
    "responseRoot": "Search"   ← optional: JSON key to extract list from
  },
  "type": "scroll",
  "children": [ <ComponentNode> ]
}

## DataSource Binding Flow (Movies feature)
1. SDUIRenderer loads ScreenModel from local JSON asset
2. ViewModel reads screenModel.dataSource
3. ViewModel calls DataSourceUseCase → Retrofit → OMDb API
4. API response mapped to flat key-value Map<String, Any>
5. SDUIRenderer re-renders with data bound via {{templateKey}} substitution
6. For lists: listDataBinding extracts array, renders itemLayout per item
7. For generatedList: countBinding reads integer N, renders itemLayout N times with {{index}}

## Template Binding Rules
- "dataBinding": "key"           → binds value directly
- "template": "{{key}} text"     → string interpolation
- "titleTemplate": "{{title}}"   → header title
- "subtitleTemplate": "{{year}} • {{genre}}" → header subtitle
- "visibility.dataBinding"       → show/hide based on value presence
- "countBinding": "totalSeasons" → generatedList item count

## MVI Architecture Per Feature
┌─────────────────────────────────────┐
│  Screen (Composable)                │
│  observes: State via StateFlow      │
│  emits: Intent                      │
│  collects: Effect (one-shot)        │
└──────────┬─────────────────┬────────┘
           │ Intent          │ State/Effect
     ┌─────▼─────────────────▼────────┐
     │  ViewModel extends BaseViewModel│
     │  handleIntent() → reduce()      │
     │  setState { copy(...) }         │
     │  setEffect(effect)              │
     └─────────────────────────────────┘

## Navigation Architecture
NavigationAction → NavigationEngine → NavController.navigate()
NavHost lives ONLY in :app
Features expose NavGraphBuilder extensions

## Design System
All UI primitives in :core:ui:
- MovieAppTheme (MaterialTheme wrapper)
- DesignTokens (colors, typography, spacing)
- BaseComponents: AppButton, AppTextField, AppCard, AppTopBar

## Component Types (SDUI Registry)
Layout:     column, row, card, spacer, divider
Text:       text, header
Media:      image, icon
Input:      textField, button
Data:       list, generatedList
Navigation: tabBar

## Color Tokens (mapped in SDUIRenderer)
screenBackground → Color(0xFF0D0F14)   dark background
cardBackground   → Color(0xFF1A1D27)   card surface
surface          → Color(0xFF1E2132)   section surface
primaryText      → Color(0xFFFFFFFF)   white
secondaryText    → Color(0xFFAAAAAA)   muted
accent           → Color(0xFFE05C5C)   red accent (IMDb star color)
