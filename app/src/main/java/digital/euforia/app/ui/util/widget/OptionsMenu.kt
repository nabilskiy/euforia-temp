package digital.euforia.app.ui.util.widget

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import digital.euforia.app.R
import digital.euforia.app.ui.theme.NavBarBackground
import digital.euforia.app.ui.theme.Red
import digital.euforia.app.ui.theme.White
import digital.euforia.app.ui.util.LocalLocalizedRes
import digital.euforia.app.ui.util.openAboutEuforia


@Composable
fun OptionsMenu(
    expanded: Boolean, onExpandedChange: (Boolean) -> Unit, menuItems: List<MenuItem>
) {
    val localizedRes = LocalLocalizedRes.current
    var stack by remember(expanded, menuItems) { mutableStateOf(listOf(menuItems)) }
    val currentMenu = stack.lastOrNull().orEmpty()
    DropdownMenu(
        containerColor = NavBarBackground,
        shape = RoundedCornerShape(20.dp),
        expanded = expanded,
        onDismissRequest = {
            onExpandedChange(false)
            stack = listOf(menuItems)
        }
    ) {
        if (stack.size > 1) {
            DropdownMenuItem(
                text = {
                    Text(
                        text = localizedRes.string(R.string.back),
                        color = White,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Normal)
                    )
                },
                onClick = {
                    stack = stack.dropLast(1)
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = null,
                        tint = White
                    )
                }
            )
            Box(
                modifier = Modifier.fillMaxWidth().height(1.dp)
                    .background(White.copy(alpha = 0.2f))
            )
        }
        currentMenu.forEachIndexed { index, item ->
            DropdownMenuItem(
                text = {
                    Text(
                        text = localizedRes.string(item.titleRes),
                        color = item.color,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Normal)
                    )
                },
                trailingIcon = {
                    when {
                        item.children.isNotEmpty() -> Icon(
                            modifier = Modifier.size(20.dp),
                            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                            contentDescription = null,
                            tint = item.color
                        )
                        item.iconRes != null -> Icon(
                            modifier = Modifier.size(24.dp),
                            painter = painterResource(id = item.iconRes),
                            contentDescription = null,
                            tint = item.color
                        )
                    }
                },
                onClick = {
                    if (item.children.isNotEmpty()) {
                        stack = stack + listOf(item.children)
                    } else {
                        item.onClick()
                        onExpandedChange(false)
                        stack = listOf(menuItems)
                    }
                }
            )
            if (index != currentMenu.lastIndex) {
                Box(
                    modifier = Modifier.fillMaxWidth().height(1.dp)
                        .background(White.copy(alpha = 0.2f))
                )
            }
        }
    }
}

@Composable
fun PublicationOptionMenu(
    expanded: Boolean,
    onExpandedChange: (Boolean) -> Unit,
    isFavourite: Boolean = false,
    onAddFavouriteClick: () -> Unit,
    onShareClick: () -> Unit,
    onReportErrorClick: (() -> Unit)? = null
) {
    val context = LocalContext.current
    Box {
        IconButton(onClick = { onExpandedChange(!expanded) }) {
            Icon(
                painter = painterResource(R.drawable.ic_menu),
                contentDescription = "Menu",
                tint = White
            )
        }

        OptionsMenu(
            expanded = expanded,
            onExpandedChange = onExpandedChange,
            menuItems = listOfNotNull(
                MenuItem(
                    titleRes = R.string.add_to_favorites,
                    iconRes = if (isFavourite) R.drawable.ic_heart_filled
                    else R.drawable.ic_heart,
                    onClick = onAddFavouriteClick
                ),
                MenuItem(
                    titleRes = R.string.share,
                    iconRes = R.drawable.ic_share,
                    onClick = onShareClick
                ),
                onReportErrorClick?.let {
                    MenuItem(
                        titleRes = R.string.report_error,
                        color = Red,
                        onClick = it
                    )
                }
            )
        )
    }
}

@Composable
fun ProgramOptionMenu(
    onAboutClick: () -> Unit,
    onShareClick: () -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }
    Box {
        IconButton(onClick = { expanded = !expanded }) {
            Icon(
                painter = painterResource(R.drawable.ic_menu),
                contentDescription = "Menu",
                tint = White
            )
        }

        OptionsMenu(
            expanded = expanded,
            onExpandedChange = { expanded = it },
            menuItems = listOf(
                MenuItem(
                    titleRes = R.string.package_menu_about,
                    iconRes = R.drawable.ic_info,
                    onClick = onAboutClick
                ),
                MenuItem(
                    titleRes = R.string.share,
                    iconRes = R.drawable.ic_share,
                    onClick = onShareClick
                )
            )
        )
    }
}

data class MenuItem(
    val titleRes: Int,
    val iconRes: Int? = null,
    val color: Color = White,
    val children: List<MenuItem> = emptyList(),
    val onClick: () -> Unit
)