# Kotlin / Compose Error Fix Prompt
# Paste the exact error output after the divider below, then run:
# cat prompts/CONTEXT.md prompts/fixes/error_fix.md | claude

## Instructions for Claude:
You are fixing a Kotlin/Compose compilation or runtime error in the MovieApp project.

Rules:
1. Read CONTEXT.md to understand the full project structure
2. Fix ONLY the file(s) causing the error
3. Do NOT change architecture, add new features, or refactor unrelated code
4. Preserve all existing patterns (MVI, SDUI, Hilt, naming conventions)
5. If the error is in a generated file, fix it minimally
6. Output only the corrected file content with the file path as a comment on line 1

## Error to fix:
--- PASTE ERROR BELOW THIS LINE ---
