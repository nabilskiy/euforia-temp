@file:OptIn(ExperimentalSharedTransitionApi::class)

package digital.euforia.app.ui.home

import androidx.annotation.DrawableRes
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import timber.log.Timber
import digital.euforia.app.R
import digital.euforia.app.data.analytics.AnalyticSender
import digital.euforia.app.domain.model.home.NavBarItem
import digital.euforia.app.ui.navigation.HomeDestination
import digital.euforia.app.ui.theme.NavBarBackground
import digital.euforia.app.ui.theme.NavBarIcon
import digital.euforia.app.ui.theme.White
import digital.euforia.app.ui.util.widget.MaxBadge
import digital.euforia.app.ui.util.widget.noRippleClickable
import org.orbitmvi.orbit.compose.collectAsState
import org.orbitmvi.orbit.compose.collectSideEffect

val NavBarHeight = 50.dp

@Composable
fun HomeScreen(
    navController: NavHostController,
    viewModel: HomeViewModel,
    isBottomBarShown: MutableState<Boolean>
) {
    val state by viewModel.collectAsState()
    viewModel.collectSideEffect { sideEffect ->
        handleSideEffect(sideEffect, navController)
    }

    HomeContent(
        navController = navController,
        isBottomBarShown = isBottomBarShown,
        navItems = state.navBarItems,
        selectedIndex = state.selectedItemIndex,
        analyticSender = viewModel.analyticSender,
        onNavItemSelected = viewModel::onNavBarItemSelected
    )
}

@Composable
private fun HomeContent(
    navController: NavHostController,
    isBottomBarShown: MutableState<Boolean>,
    navItems: List<NavBarItem>,
    selectedIndex: Int,
    analyticSender: AnalyticSender,
    onNavItemSelected: (Int) -> Unit
) {
    Box(modifier = Modifier.fillMaxSize()) {
        // Shared NavHost handles the screens now.
        // We only show the BottomNavigation here.
        AnimatedVisibility(
            visible = isBottomBarShown.value,
            enter = fadeIn(animationSpec = tween(durationMillis = 300)),
            exit = fadeOut(animationSpec = tween(durationMillis = 300)),
            modifier = Modifier.align(Alignment.BottomCenter)
        ) {
            BottomNavigation(
                navController = navController,
                items = navItems,
                selectedIndex = selectedIndex,
                analyticSender = analyticSender,
                onNavItemSelected = onNavItemSelected
            )
        }
    }
}

@Composable
fun BoxScope.BottomNavigation(
    navController: NavHostController,
    items: List<NavBarItem>,
    selectedIndex: Int,
    analyticSender: AnalyticSender,
    onNavItemSelected: (Int) -> Unit
) {
    // Keep selected index in sync with current destination (also on system back)
    LaunchedEffect(navController, items, selectedIndex) {
        navController.currentBackStackEntryFlow.collect { backStackEntry ->
            val dest = backStackEntry.destination
            // Find index of the nav item whose route matches current destination
            val route = dest.route?.substringBefore("?")
            Timber.tag("NAVIGATION").d("BottomNavigation sync: route=$route")
            val newIndex = items.indexOfFirst { item ->
                val itemRoute = item.destination::class.qualifiedName
                route == itemRoute || (route != null && itemRoute != null && route.contains(itemRoute))
            }
            if (newIndex != -1 && newIndex != selectedIndex) {
                Timber.tag("NAVIGATION").d("Syncing nav index to $newIndex for route $route")
                onNavItemSelected(newIndex)
            }
        }
    }

    Box(Modifier.fillMaxSize()) {

        Row(
            modifier = Modifier.align(Alignment.BottomCenter).fillMaxWidth()
                .background(
                    color = NavBarBackground,
                    shape = RoundedCornerShape(topStart = 26.dp, topEnd = 26.dp)
                )
//                .padding(160.dp)
                .navigationBarsPadding()
//                .height(50.dp)
            ,
//            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            items.forEachIndexed { index, item ->
                BottomNavigationItem(
                    item = item,
                    isSelected = selectedIndex == index,
                    onClick = {
                        if (selectedIndex != index) {
                            when (item) {
                                NavBarItem.PLAN -> analyticSender.tabTodayClick()
                                NavBarItem.PROGRAMS -> analyticSender.tabLibraryClick()
                                NavBarItem.SOUNDSCAPES -> analyticSender.tabScenesClick()
                                else -> analyticSender.tabProfileClick()
                            }
                            onNavItemSelected(index)
                            navController.navigate(item.destination) {
                                // Pop up to the start destination of the graph to
                                // avoid building up a large stack of destinations
                                // on the back stack as users select items
                                popUpTo(HomeDestination.Plan::class.qualifiedName!!) {
                                    saveState = true
                                }
                                // Avoid multiple copies of the same destination when
                                // reselecting the same item
                                launchSingleTop = true
                                // Restore state when reselecting a previously selected item
                                restoreState = true
                            }
                        }
                    }
                )
            }

        }
    }
}

@Composable
fun RowScope.BottomNavigationItem(item: NavBarItem, isSelected: Boolean, onClick: () -> Unit) {
    val color by animateColorAsState(
        targetValue = if (item == NavBarItem.SETTINGS_MAX || !isSelected) {
            NavBarIcon
        } else {
            White
        },
        animationSpec = tween(500)
    )

    Box(modifier = Modifier.weight(1f).noRippleClickable { onClick() }.padding(vertical = 10.dp)) {
        Icon(
            painter = painterResource(item.iconRes),
            tint = color,
            contentDescription = null,
            modifier = Modifier.align(Alignment.Center)
        )
        if (item == NavBarItem.SETTINGS_MAX) {
            MaxBadge(Modifier.align(Alignment.BottomCenter))
        }
    }
}

private fun handleSideEffect(sideEffect: HomeSideEffect, navController: NavHostController) {
    when (sideEffect) {
        else -> {}
    }
}