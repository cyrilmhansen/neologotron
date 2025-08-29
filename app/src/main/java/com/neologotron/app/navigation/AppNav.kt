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
    data object Main : Route("main")
    data object History : Route("history")
    data object Favorites : Route("favorites")
    data object Settings : Route("settings")
    data object Thematic : Route("thematic")
    data object Workshop : Route("workshop")
    data object Debug : Route("debug")
    data object About : Route("about")
    data object Detail : Route("detail/{word}?from={from}&def={def}&decomp={decomp}") {
        const val argName = "word"
        const val fromArg = "from"
        const val defArg = "def"
        const val decompArg = "decomp"
        fun build(word: String, from: Route? = null, def: String? = null, decomp: String? = null): String {
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
