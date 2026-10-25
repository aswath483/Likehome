package com.likehome.app.ui.nav

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.listSaver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.saveable.rememberSaveableStateHolder
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.runtime.toMutableStateList

/** Every full screen in the app outside the ringing flow. */
enum class Route { Onboarding, Home, Settings, Setup, History, Styles, Voice, Faces, About, Sleep, VoiceImport, FamilyLive }

/** A back stack of [Route]s. Survives rotation and process death via [rememberNavigator]. */
@Stable
class Navigator internal constructor(private val stack: SnapshotStateList<Route>) {

    val current: Route get() = stack.last()
    val canGoBack: Boolean get() = stack.size > 1

    /** Which way the last move went, so transitions slide the right way. */
    var forward by mutableStateOf(true)
        private set

    fun go(route: Route) {
        if (route == current) return
        forward = true
        stack.add(route)
    }

    fun back(): Boolean {
        if (!canGoBack) return false
        forward = false
        stack.removeAt(stack.lastIndex)
        return true
    }

    /** Clears the stack, e.g. after onboarding. */
    fun reset(route: Route) {
        forward = true
        stack.clear()
        stack.add(route)
    }
}

@Composable
fun rememberNavigator(start: Route): Navigator {
    val stack = rememberSaveable(
        saver = listSaver<SnapshotStateList<Route>, String>(
            save = { list -> list.map { it.name } },
            restore = { names -> names.map(Route::valueOf).toMutableStateList() },
        )
    ) { mutableStateListOf(start) }
    return remember(stack) { Navigator(stack) }
}

/**
 * Shows the current route with a direction-aware slide, keeps each screen's saved state
 * (scroll position, text typed) while it is on the stack, and handles system Back.
 */
@Composable
fun NavHost(nav: Navigator, content: @Composable (Route) -> Unit) {
    val holder = rememberSaveableStateHolder()
    BackHandler(enabled = nav.canGoBack) { nav.back() }
    AnimatedContent(
        targetState = nav.current,
        transitionSpec = {
            val dir = if (nav.forward) 1 else -1
            val enter = slideInHorizontally(tween(340, easing = FastOutSlowInEasing)) { w -> dir * w / 4 } +
                fadeIn(tween(260, delayMillis = 40))
            val exit = slideOutHorizontally(tween(340, easing = FastOutSlowInEasing)) { w -> -dir * w / 10 } +
                fadeOut(tween(180))
            enter togetherWith exit
        },
        label = "nav",
    ) { route ->
        holder.SaveableStateProvider(route.name) { content(route) }
    }
}
