package ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.TooltipArea
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.window.WindowDraggableArea
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.rounded.Close
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.WindowState
import cafe.adriel.voyager.navigator.CurrentScreen
import cafe.adriel.voyager.navigator.Navigator
import database.Database
import icon
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import logger.SystemLogger
import logger.format

@Composable
fun MainWindow(
	isMainWindowOpen: MutableState<Boolean>,
) {
	Window(
		onCloseRequest = {
			isMainWindowOpen.value = false
		},
		icon = icon,
		title = "KtLogger",
		visible = isMainWindowOpen.value,
		transparent = true,
		undecorated = true,
		state = WindowState(width = 850.dp, height = 700.dp)
	) {
		Surface(
			modifier = Modifier.fillMaxSize().padding(5.dp).shadow(3.dp, RoundedCornerShape(10.dp)),
			color = MaterialTheme.colors.background,
			shape = RoundedCornerShape(10.dp)
		) {
			Column(
				modifier = Modifier.fillMaxSize()
			) {
				Navigator(HomeScreen()) {
					Scaffold(
						topBar = {
							WindowDraggableArea {
								TopAppBar(
									title = {
										if (it.canPop && it.lastItem is WindowInfoScreen)
											Row {
												val screen = it.lastItem as WindowInfoScreen
												Text(text = screen.windowName)
												Spacer(modifier = Modifier.weight(1f))
												Text(text = screen.focusTime.format())
											}
										else if (it.lastItem is HomeScreen) {
											val time = (it.lastItem as HomeScreen).timeTotal
											// Have the home text at the start and the time at the end
											Row {
												Text(text = "Home")
												Spacer(modifier = Modifier.weight(1f))
												Text(text = time.format())
											}
										}
									},
									navigationIcon = navigationIcon(it),
									actions = {
										Spacer(modifier = Modifier.padding(15.dp))
										// Icon to show if the logger is running
										val isRunning by SystemLogger.isRunning.collectAsState()
										ColorChangingDot(
											isRunning,
											"Logger is running",
											"Logger is not running"
										)
										
										// Refresh the data
										IconButton(
											onClick = {
												// Refresh the data
												CoroutineScope(Dispatchers.Default).launch {
													SystemLogger.saveData()
													Database.loadData()
												}
											},
											modifier = Modifier.padding(start = 10.dp)
										) {
											Icon(
												Icons.Filled.Refresh,
												contentDescription = "Refresh",
												tint = MaterialTheme.colors.onBackground
											)
										}
										
										// Close the window
										IconButton(onClick = {
											isMainWindowOpen.value = false
										}) {
											Icon(
												imageVector = Icons.Rounded.Close,
												contentDescription = "Close",
												tint = MaterialTheme.colors.onBackground
											)
										}
									}
								)
							}
						},
						content = {
							CurrentScreen()
						}
					)
				}
			}
		}
	}
}

@Composable
private fun navigationIcon(navigator: Navigator): (@Composable () -> Unit)? {
	if (navigator.canPop) {
		return {
			IconButton(onClick = {
				navigator.pop()
			}) {
				Icon(
					imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
					contentDescription = "Back"
				)
			}
		}
	}
	return null
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ColorChangingDot(condition: Boolean, tooltipPositive: String, tooltipNegative: String) {
	val dotColor = if (condition) Color.Green else Color.Red
	TooltipArea(
		tooltip = {
			Box(
				modifier = Modifier
					.background(
						MaterialTheme.colors.surface,
						shape = RoundedCornerShape(10.dp)
					)
					.padding(10.dp),
			) {
				Text(if (condition) tooltipPositive else tooltipNegative)
			}
		},
		delayMillis = 250
	) {
		Canvas(modifier = Modifier.size(10.dp)) {
			drawCircle(color = dotColor)
		}
	}
}
