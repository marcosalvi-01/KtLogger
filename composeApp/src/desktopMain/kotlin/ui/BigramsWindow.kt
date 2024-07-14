package ui

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.window.WindowDraggableArea
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.WindowState
import icon
import keyboard.KC

@Composable
@Preview
fun BigramsWindow(
	isBigramsOpen: MutableState<Boolean>,
	bigrams: Map<Pair<KC, KC>, Int>,
) {
	val windowState = WindowState(width = 550.dp, height = 700.dp)

	Window(
		onCloseRequest = {
			isBigramsOpen.value = false
		},
		state = windowState,
		title = "Bigrams",
		icon = icon,
		visible = isBigramsOpen.value,
		transparent = true,
		undecorated = true
	) {
		Surface(
			modifier = Modifier.fillMaxSize().padding(5.dp).shadow(3.dp, RoundedCornerShape(10.dp)),
			color = MaterialTheme.colors.background,
			shape = RoundedCornerShape(10.dp)
		) {
			Column(
				modifier = Modifier.fillMaxSize()
			) {
				WindowDraggableArea {
					TopAppBar(
						title = { Text("Bigrams") },
						actions = {
							// Close the window
							IconButton(onClick = {
								isBigramsOpen.value = false
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

				var bigramsToShow: Map<Pair<KC, KC>, Int> by remember { mutableStateOf(bigrams) }

				val bigramsTotal = bigramsToShow.values.sum()

				val showOnlyLetters = remember { mutableStateOf(false) }

				Button(
					modifier = Modifier.align(Alignment.CenterHorizontally),
					onClick = {
						showOnlyLetters.value = !showOnlyLetters.value
						// filter the bigrams to show only the ones with letters
						bigramsToShow = if (showOnlyLetters.value) {
							bigrams.filter { (bigram, _) ->
								bigram.first.isLetter() && bigram.second.isLetter()
							}
						} else {
							bigrams
						}
					},
					colors = if (showOnlyLetters.value) {
						ButtonDefaults.buttonColors(backgroundColor = MaterialTheme.colors.secondary)
					} else {
						ButtonDefaults.buttonColors()
					}
				) {
					Text("Show only letters")
				}

				Table(
					columns = listOf(
						KeyColumnItem("Bigram", 0.5f),
						KeyColumnItem("Count", 0.5f),
						KeyColumnItem("Percentage", 0.5f),
					),
					showScrollbar = true,
					data = bigramsToShow.entries.sortedByDescending { it.value }
						.map { (bigram, value) ->
							KeyRowItem(
								listOf(
									KeyColumnItem(
										bigram.first.unicode + " " + bigram.second.unicode,
										0.5f
									),
									KeyColumnItem(value.toString(), 0.5f),
									KeyColumnItem(
										"${
											String.format(
												"%.2f",
												(value.toDouble() / bigramsTotal * 100)
											)
										}%",
										0.5f
									)
								)
							)
						}) {

					for (column in it.getColumns()) {
						TableCell(text = column.getName(), weight = column.getWeight())
					}
				}
			}
		}
	}
}
