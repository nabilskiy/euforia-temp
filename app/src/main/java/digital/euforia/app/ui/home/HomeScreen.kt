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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import digital.euforia.app.R
import digital.euforia.app.domain.model.home.NavBarItem
import digital.euforia.app.ui.navigation.HomeDestination
import digital.euforia.app.ui.navigation.HomeNavigation
import digital.euforia.app.ui.theme.NavBarBackground
import digital.euforia.app.ui.theme.NavBarIcon
import digital.euforia.app.ui.theme.White
import digital.euforia.app.ui.util.widget.noRippleClickable
import org.orbitmvi.orbit.compose.collectAsState
import org.orbitmvi.orbit.compose.collectSideEffect

val NavBarHeight = 50.dp

@Composable
fun HomeScreen(
    viewModel: HomeViewModel
) {
    val navController = rememberNavController()
    val state by viewModel.collectAsState()
    viewModel.collectSideEffect { sideEffect ->
        handleSideEffect(sideEffect)
    }

    HomeContent(
        navController = navController,
        navItems = state.navBarItems,
        selectedIndex = state.selectedItemIndex,
        onNavItemSelected = viewModel::onNavBarItemSelected
    )
}

@Composable
private fun HomeContent(
    navController: NavHostController,
    navItems: List<NavBarItem>,
    selectedIndex: Int,
    onNavItemSelected: (Int) -> Unit
) {
    val isBottomBarShown = remember { mutableStateOf(true) }
    Box(modifier = Modifier.fillMaxSize()) {
        HomeNavigation(navController, isBottomBarShown)
        AnimatedVisibility(
            visible = isBottomBarShown.value,
            enter = fadeIn(animationSpec = tween(durationMillis = 300)),
            exit = fadeOut(animationSpec = tween(durationMillis = 300)),
        ) {
            BottomNavigation(
                navController = navController,
                items = navItems,
                selectedIndex = selectedIndex,
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
    onNavItemSelected: (Int) -> Unit
) {
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
                            onNavItemSelected(index)
                            navController.navigate(item.destination) {
                                // Pop up to the start destination of the graph to
                                // avoid building up a large stack of destinations
                                // on the back stack as users select items
                                popUpTo(navController.graph.startDestinationId) {
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
    }

}

private fun handleSideEffect(sideEffect: HomeSideEffect) {
    when (sideEffect) {
        else -> {}
    }
}