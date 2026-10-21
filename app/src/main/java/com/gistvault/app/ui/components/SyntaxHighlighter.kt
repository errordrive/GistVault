package com.gistvault.app.ui.components

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight

fun highlightCode(code: String, language: String?, isDark: Boolean): AnnotatedString {
    if (code.isEmpty()) return AnnotatedString("")

    val keywordColor = if (isDark) Color(0xFFFF7B72) else Color(0xFFCF222E)
    val stringColor = if (isDark) Color(0xFFA5D6FF) else Color(0xFF0A3069)
    val numberColor = if (isDark) Color(0xFF79C0FF) else Color(0xFF0550AE)
    val commentColor = if (isDark) Color(0xFF8B949E) else Color(0xFF6E7781)
    val booleanColor = if (isDark) Color(0xFFFFA657) else Color(0xFF953800)
    val jsonKeyColor = if (isDark) Color(0xFF7EE787) else Color(0xFF116329)
    val defaultColor = if (isDark) Color(0xFFE6EDF3) else Color(0xFF1F2328)

    val keywords = setOf(
        "fun", "val", "var", "class", "interface", "object", "package", "import",
        "return", "if", "else", "when", "for", "while", "do", "try", "catch", "finally",
        "throw", "public", "private", "protected", "internal", "override", "open", "final",
        "abstract", "sealed", "data", "enum", "const", "let", "function", "async", "await",
        "def", "import", "from", "as", "self", "this", "super", "yield", "switch", "case",
        "break", "continue", "default", "struct", "impl", "type", "select", "from", "where"
    )

    return buildAnnotatedString {
        append(code)
        addStyle(SpanStyle(color = defaultColor), 0, code.length)

        // 1. Strings
        val stringRegex = Regex("\"(\\\\.|[^\"\\\\])*\"|'(\\\\.|[^'\\\\])*'")
        for (match in stringRegex.findAll(code)) {
            // Check if it's a JSON key followed by a colon
            val endIdx = match.range.last + 1
            val isJsonKey = endIdx < code.length && code.substring(endIdx).trimStart().startsWith(":")
            val color = if (isJsonKey) jsonKeyColor else stringColor
            addStyle(SpanStyle(color = color), match.range.first, endIdx)
        }

        // 2. Comments
        val commentRegex = Regex("(//.*)|(#.*)")
        for (match in commentRegex.findAll(code)) {
            addStyle(SpanStyle(color = commentColor), match.range.first, match.range.last + 1)
        }

        // 3. Numbers
        val numberRegex = Regex("\\b\\d+(\\.\\d+)?\\b")
        for (match in numberRegex.findAll(code)) {
            addStyle(SpanStyle(color = numberColor), match.range.first, match.range.last + 1)
        }

        // 4. Booleans / Null
        val boolRegex = Regex("\\b(true|false|null|nil|undefined)\\b")
        for (match in boolRegex.findAll(code)) {
            addStyle(SpanStyle(color = booleanColor, fontWeight = FontWeight.SemiBold), match.range.first, match.range.last + 1)
        }

        // 5. Keywords
        val wordRegex = Regex("\\b[a-zA-Z_][a-zA-Z0-9_]*\\b")
        for (match in wordRegex.findAll(code)) {
            if (keywords.contains(match.value)) {
                addStyle(SpanStyle(color = keywordColor, fontWeight = FontWeight.Bold), match.range.first, match.range.last + 1)
            }
        }
    }
}
