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
    data object Detail : Route("detail/{word}") {
        const val argName = "word"
        fun build(word: String): String = "detail/${Uri.encode(word)}"
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
