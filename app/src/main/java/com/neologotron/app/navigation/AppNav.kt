package com.neologotron.app.navigation

import android.net.Uri
import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Route(val value: String) {
    data object Onboarding : Route("onboarding")
    data object Main : Route("main")
    data object History : Route("history")
    data object Favorites : Route("favorites")
    data object Settings : Route("settings")
    data object Thematic : Route("thematic")
    data object Workshop : Route("workshop")
    data object Debug : Route("debug")
    data object About : Route("about")
    data object Detail : Route(
        "detail/{word}?from={from}&def={def}&decomp={decomp}" +
            "&pform={pform}&rform={rform}&sform={sform}" +
            "&rgloss={rgloss}&rconn={rconn}&spos={spos}&sdef={sdef}&stags={stags}"
    ) {
        const val argName = "word"
        const val fromArg = "from"
        const val defArg = "def"
        const val decompArg = "decomp"
        const val pformArg = "pform"
        const val rformArg = "rform"
        const val sformArg = "sform"
        const val rglossArg = "rgloss"
        const val rconnArg = "rconn"
        const val sposArg = "spos"
        const val sdefArg = "sdef"
        const val stagsArg = "stags"
        fun build(
            word: String,
            from: Route? = null,
            def: String? = null,
            decomp: String? = null,
            pform: String? = null,
            rform: String? = null,
            sform: String? = null,
            rgloss: String? = null,
            rconn: String? = null,
            spos: String? = null,
            sdef: String? = null,
            stags: String? = null,
        ): String {
            val f = when (from) {
                is Main -> Main.value
                is History -> History.value
                is Favorites -> Favorites.value
                is Settings -> Settings.value
                is Thematic -> Thematic.value
                is Workshop -> Workshop.value
                is Debug -> Debug.value
                else -> ""
            }
            val base = "detail/${Uri.encode(word)}"
            val params = mutableListOf<String>()
            if (f.isNotBlank()) params += "from=$f"
            def?.takeIf { it.isNotBlank() }?.let { params += "def=${Uri.encode(it)}" }
            decomp?.takeIf { it.isNotBlank() }?.let { params += "decomp=${Uri.encode(it)}" }
            pform?.takeIf { it.isNotBlank() }?.let { params += "pform=${Uri.encode(it)}" }
            rform?.takeIf { it.isNotBlank() }?.let { params += "rform=${Uri.encode(it)}" }
            sform?.takeIf { it.isNotBlank() }?.let { params += "sform=${Uri.encode(it)}" }
            rgloss?.takeIf { it.isNotBlank() }?.let { params += "rgloss=${Uri.encode(it)}" }
            rconn?.takeIf { it.isNotBlank() }?.let { params += "rconn=${Uri.encode(it)}" }
            spos?.takeIf { it.isNotBlank() }?.let { params += "spos=${Uri.encode(it)}" }
            sdef?.takeIf { it.isNotBlank() }?.let { params += "sdef=${Uri.encode(it)}" }
            stags?.takeIf { it.isNotBlank() }?.let { params += "stags=${Uri.encode(it)}" }
            return if (params.isEmpty()) base else base + "?" + params.joinToString("&")
        }
    }
}

data class NavItem(
    val route: Route,
    @StringRes val titleRes: Int,
    val icon: ImageVector
)

object NavItems {
    val bottomBar = listOf(
        NavItem(Route.Main, com.neologotron.app.R.string.title_main, Icons.Outlined.Home),
        NavItem(Route.History, com.neologotron.app.R.string.title_history, Icons.Outlined.History),
        NavItem(Route.Favorites, com.neologotron.app.R.string.title_favorites, Icons.Outlined.FavoriteBorder),
        NavItem(Route.Settings, com.neologotron.app.R.string.title_settings, Icons.Outlined.Settings)
    )
}
