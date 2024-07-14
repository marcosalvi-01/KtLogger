package ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.window.WindowDraggableArea
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.WindowState
import icon

@Composable
fun AppWindow(
	isOpen: MutableState<Boolean>,
	title: String,
	width: Dp = 1000.dp,
	height: Dp = 600.dp,
	content: @Composable () -> Unit,
) {
	val windowState = WindowState(width = width, height = height)
	Window(
		onCloseRequest = { isOpen.value = false },
		state = windowState,
		title = title,
		icon = icon,
		visible = isOpen.value,
		transparent = true,
		undecorated = true
	) {
		Surface(
			modifier = Modifier.fillMaxSize().padding(5.dp).shadow(3.dp, RoundedCornerShape(10.dp)),
			color = MaterialTheme.colors.background,
			shape = RoundedCornerShape(10.dp)
		) {
			Column(modifier = Modifier.fillMaxSize()) {
				WindowDraggableArea {
					TopAppBar(title = { Text(title) }, actions = {
						IconButton(onClick = { isOpen.value = false }) {
							Icon(
								imageVector = Icons.Rounded.Close,
								contentDescription = "Close",
								tint = MaterialTheme.colors.onBackground
							)
						}
					})
				}
				content()
			}
		}
	}
}
