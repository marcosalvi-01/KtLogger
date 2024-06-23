package ui

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.window.WindowDraggableArea
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.runtime.*
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
fun TrigramsWindow(
	isTrigramsOpen: MutableState<Boolean>,
	trigrams: Map<Triple<KC, KC, KC>, Int>,
) {
	val windowState = WindowState(width = 650.dp, height = 700.dp)

	Window(
		onCloseRequest = {
			isTrigramsOpen.value = false
		},
		state = windowState,
		title = "Bigrams",
		icon = icon,
		visible = isTrigramsOpen.value,
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
						title = { Text("Trigrams") },
						actions = {
							// Close the window
							IconButton(onClick = {
								isTrigramsOpen.value = false
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

				var trigramsToShow: Map<Triple<KC, KC, KC>, Int> by remember {
					mutableStateOf(
						trigrams
					)
				}

				val trigramsTotal = trigramsToShow.values.sum()

				val showOnlyLetters = remember { mutableStateOf(false) }

				Button(
					modifier = Modifier.align(Alignment.CenterHorizontally),
					onClick = {
						showOnlyLetters.value = !showOnlyLetters.value
						// filter the bigrams to show only the ones with letters
						trigramsToShow = if (showOnlyLetters.value) {
							trigrams.filter { (bigram, _) ->
								bigram.first.isLetter() && bigram.second.isLetter() && bigram.third.isLetter()
							}
						} else {
							trigrams
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
						KeyColumnItem("Trigram", 0.5f),
						KeyColumnItem("Count", 0.5f),
						KeyColumnItem("Percentage", 0.5f),
					),
					showScrollbar = true,
					data = trigramsToShow.entries.sortedByDescending { it.value }
						.map { (trigram, value) ->
							KeyRowItem(
								listOf(
									KeyColumnItem(
										trigram.first.symbol + " " + trigram.second.symbol + " " + trigram.third.symbol,
										0.5f
									),
									KeyColumnItem(value.toString(), 0.5f),
									KeyColumnItem(
										"${
											String.format(
												"%.2f",
												(value.toDouble() / trigramsTotal * 100)
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
