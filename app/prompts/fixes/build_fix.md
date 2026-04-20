# Gradle Build Error Fix Prompt
# Paste the gradle error output after the divider below, then run:
# cat prompts/CONTEXT.md prompts/fixes/build_fix.md | claude

## Instructions for Claude:
You are fixing a Gradle build error in the MovieApp project.

Rules:
1. Read CONTEXT.md to understand the module structure
2. Fix ONLY the build.gradle.kts or settings.gradle.kts causing the error
3. Do NOT change any Kotlin source files unless the error is a missing dependency
4. Check: missing module include in settings.gradle.kts
5. Check: missing dependency in the affected module's build.gradle.kts
6. Check: version catalog (libs.versions.toml) for missing library aliases
7. Output the corrected file(s) with file path as comment on line 1

## Build error to fix:
--- PASTE GRADLE OUTPUT BELOW THIS LINE ---
