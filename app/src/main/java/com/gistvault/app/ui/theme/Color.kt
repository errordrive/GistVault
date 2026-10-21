package com.gistvault.app.ui.theme

import androidx.compose.ui.graphics.Color

// Shared / Language colors matching GitHub linguist
val LangJs = Color(0xFFF1E05A)
val LangTs = Color(0xFF3178C6)
val LangPython = Color(0xFF3572A5)
val LangJava = Color(0xFFB07219)
val LangCpp = Color(0xFFF34B7D)
val LangC = Color(0xFF555759)
val LangCSharp = Color(0xFF178600)
val LangGo = Color(0xFF00ADD8)
val LangRust = Color(0xFFDEA584)
val LangRuby = Color(0xFF701516)
val LangPhp = Color(0xFF4F5D95)
val LangSwift = Color(0xFFF05138)
val LangKotlin = Color(0xFFA97BFF)
val LangHtml = Color(0xFFE34C26)
val LangCss = Color(0xFF563D7C)
val LangScss = Color(0xFFC6538C)
val LangJson = Color(0xFFCBCB41)
val LangYaml = Color(0xFFCB171E)
val LangShell = Color(0xFF3FA34D)
val LangMarkdown = Color(0xFF083FA1)
val LangSql = Color(0xFFE38C00)
val LangDart = Color(0xFF00B4AB)
val LangVue = Color(0xFF41B883)
val LangDockerfile = Color(0xFF384D54)
val LangText = Color(0xFF8B949E)

fun getLanguageColor(language: String?): Color {
    return when (language?.trim()) {
        "JavaScript" -> LangJs
        "TypeScript" -> LangTs
        "Python" -> LangPython
        "Java" -> LangJava
        "C++" -> LangCpp
        "C" -> LangC
        "C#" -> LangCSharp
        "Go" -> LangGo
        "Rust" -> LangRust
        "Ruby" -> LangRuby
        "PHP" -> LangPhp
        "Swift" -> LangSwift
        "Kotlin" -> LangKotlin
        "HTML" -> LangHtml
        "CSS" -> LangCss
        "SCSS" -> LangScss
        "JSON" -> LangJson
        "YAML" -> LangYaml
        "Shell" -> LangShell
        "Markdown" -> LangMarkdown
        "SQL" -> LangSql
        "Dart" -> LangDart
        "Vue" -> LangVue
        "Dockerfile" -> LangDockerfile
        else -> LangText
    }
}

// Theme Palettes
data class GistVaultColors(
    val paper: Color,
    val card: Color,
    val ink: Color,
    val inkSoft: Color,
    val muted: Color,
    val faint: Color,
    val line: Color,
    val primary: Color,
    val onPrimary: Color,
    val primaryDark: Color,
    val primarySoft: Color,
    val success: Color,
    val successSoft: Color,
    val danger: Color,
    val dangerSoft: Color,
    val amber: Color,
    val amberSoft: Color,
    val purple: Color,
    val purpleSoft: Color,
    val isDark: Boolean
)

val LightPalette = GistVaultColors(
    paper = Color(0xFFF6F8FA),
    card = Color(0xFFFFFFFF),
    ink = Color(0xFF1F2328),
    inkSoft = Color(0xFF32383F),
    muted = Color(0xFF656D76),
    faint = Color(0xFF8C959F),
    line = Color(0xFFD0D7DE),
    primary = Color(0xFF0969DA),
    onPrimary = Color(0xFFFFFFFF),
    primaryDark = Color(0xFF0550AE),
    primarySoft = Color(0xFFE8F1FC),
    success = Color(0xFF1A7F37),
    successSoft = Color(0xFFE6F6EB),
    danger = Color(0xFFD1242F),
    dangerSoft = Color(0xFFFFECEE),
    amber = Color(0xFF9A6700),
    amberSoft = Color(0xFFFFF8C5),
    purple = Color(0xFF8250DF),
    purpleSoft = Color(0xFFFBEFFF),
    isDark = false
)

// Premium Dark Navy inspired by Reference Design
val DarkPalette = GistVaultColors(
    paper = Color(0xFF090E17),
    card = Color(0xFF121C2F),
    ink = Color(0xFFF0F6FC),
    inkSoft = Color(0xFFC9D1D9),
    muted = Color(0xFF8B949E),
    faint = Color(0xFF48546A),
    line = Color(0xFF1E2D4A),
    primary = Color(0xFF2563EB),
    onPrimary = Color(0xFFFFFFFF),
    primaryDark = Color(0xFF1D4ED8),
    primarySoft = Color(0xFF152A4E),
    success = Color(0xFF2EA043),
    successSoft = Color(0xFF0D281E),
    danger = Color(0xFFF85149),
    dangerSoft = Color(0xFF331518),
    amber = Color(0xFFE3B341),
    amberSoft = Color(0xFF2F2410),
    purple = Color(0xFFA371F7),
    purpleSoft = Color(0xFF261B3D),
    isDark = true
)

val PinkPalette = GistVaultColors(
    paper = Color(0xFFFFF6F9),
    card = Color(0xFFFFFFFF),
    ink = Color(0xFF2B1A2E),
    inkSoft = Color(0xFF4A2C4E),
    muted = Color(0xFF9A7B9E),
    faint = Color(0xFFC4A9C7),
    line = Color(0xFFF5E1EC),
    primary = Color(0xFFE84393),
    onPrimary = Color(0xFFFFFFFF),
    primaryDark = Color(0xFFC9187A),
    primarySoft = Color(0xFFFDE8F2),
    success = Color(0xFF16A34A),
    successSoft = Color(0xFFE5F6EB),
    danger = Color(0xFFDC2626),
    dangerSoft = Color(0xFFFDECEC),
    amber = Color(0xFFD97706),
    amberSoft = Color(0xFFFDF0DC),
    purple = Color(0xFFA855F7),
    purpleSoft = Color(0xFFF5E9FD),
    isDark = false
)
