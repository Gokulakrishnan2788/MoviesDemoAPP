# SDUI Contract
# ALL screens must be driven by this JSON schema. No exceptions.

## Screen JSON Top-Level Schema
{
  "screenId": "string",
  "dataSource": {                      ← optional block — ViewModel reads this to fetch live data
    "type": "remote",
    "method": "GET",
    "url": "https://api.example.com/endpoint/{{param}}",
    "responseRoot": "Search",          ← optional: extract sub-key from response JSON
    "enrichmentDataSource": {          ← optional: per-item detail fetch
      "type": "remote",
      "method": "GET",
      "url": "https://api.example.com/detail/{{imdbID}}"
    }
  },
  "type": "scroll" | "column" | "lazyColumn",
  "children": [ <ComponentNode> ]
}

## ComponentNode Schema
{
  "id": "unique_id",
  "type": "ComponentType",
  "style": { <StyleProps> },
  "children": [ <ComponentNode> ],     ← for layout types
  "dataBinding": "keyName",            ← binds single value from data map
  "template": "{{key}} other text",    ← string interpolation
  "titleTemplate": "{{key}}",          ← for header type
  "subtitleTemplate": "{{k1}} • {{k2}}",
  "text": "static string",             ← static text (no binding)
  "icon": "sf_symbol_name",            ← Material icon name (e.g. star, play_arrow)
  "listDataBinding": "keyName",        ← for list type: binds array
  "countBinding": "keyName",           ← for generatedList: binds integer count
  "itemLayout": { <ComponentNode> },   ← template for list/generatedList items
  "action": "ACTION_ID",               ← tappable action reference
  "visibility": {
    "dataBinding": "key",
    "isNotEmpty": true
  }
}

## Supported Component Types
| Type          | Description                                          |
|---------------|------------------------------------------------------|
| column        | Vertical layout container                            |
| row           | Horizontal layout container                          |
| card          | Elevated surface container                           |
| text          | Text with dataBinding or template or static text     |
| header        | Large title + subtitle (titleTemplate/subtitleTemplate)|
| image         | Async image via Coil (dataBinding for URL)           |
| icon          | Material icon by name                                |
| button        | Tappable button with label                           |
| spacer        | Empty space (height in style)                        |
| divider       | Horizontal rule                                      |
| list          | Scrollable list bound to array (listDataBinding)     |
| generatedList | Generated N rows from integer count (countBinding)   |

## StyleProps
{
  "padding": 16,                  → uniform padding dp
  "paddingHorizontal": 16,
  "paddingVertical": 8,
  "spacing": 12,                  → arrangement spacing dp (column/row)
  "backgroundColor": "tokenName", → color token string
  "foregroundColor": "tokenName", → text/icon color token
  "cornerRadius": 18,             → shape dp
  "fontSize": 15,
  "fontWeight": "bold|semibold|medium|normal",
  "lineLimit": 2,                 → maxLines
  "frameWidth": 88,               → fixed width dp
  "frameHeight": 128              → fixed height dp
}

## Color Token Map (resolved in SDUIRenderer)
"screenBackground" → Color(0xFF0D0F14)
"cardBackground"   → Color(0xFF1A1D27)
"surface"          → Color(0xFF1E2132)
"primaryText"      → Color(0xFFFFFFFF)
"secondaryText"    → Color(0xFFAAAAAA)
"accent"           → Color(0xFFE05C5C)

## Template Binding Rules
- {{key}} in any string field → replaced with value from data map
- Keys are case-sensitive and match the API response field names (lowercased by mapper)
- Missing keys → render empty string (never crash)
- generatedList items get special key: {{seasonNumber}} = current 1-based index

## DataSource Flow (ViewModel responsibility — NOT renderer)
1. Load ScreenModel from local JSON asset (assets/screens/X.json)
2. Read screenModel.dataSource
3. Build URL (replace {{param}} with navigation args)
4. Call repository → Retrofit → parse JSON
5. If responseRoot present → extract that key as list
6. If enrichmentDataSource present → fetch detail per item using item's imdbID
7. Map response to Map<String, String> (flat) or List<Map<String,String>> (for list)
8. Update state with boundData / boundList
9. SDUIRenderer re-renders with resolved bindings

## List Screen JSON Reference (tv_series_list.json)
- listDataBinding: "series" → bound to List<Map<String,String>>
- Each item map has keys: title, year, type, posterURL, rating, genre, imdbID
- action: "seriesTapped" → navigates to series_detail with imdbID param

## Detail Screen JSON Reference (series_detail.json)
- dataSource.url: "https://www.omdbapi.com/?i={{seriesId}}&apikey=8170cd9d"
- Bound keys: title, year, genre, posterURL, rating, runtime, totalSeasons, awards, plot, actors, writer, director
- generatedList with countBinding: "totalSeasons" → renders Season 1..N rows
