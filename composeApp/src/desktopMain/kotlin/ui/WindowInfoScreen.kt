package ui

import androidx.compose.foundation.layout.*
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import keyboard.KC
import logger.MouseButton
import logger.Position
import logger.ScrollDirection
import kotlin.time.Duration

data class WindowInfoScreen(
	val windowName: String,
	val focusTime: Duration,
	val keyPresses: Map<KC, Int>,
	val bigrams: Map<Pair<KC, KC>, Int>,
	val trigrams: Map<Triple<KC, KC, KC>, Int>,
	val mouseButtonPresses: Map<MouseButton, Int>,
	val scrollDirections: Map<ScrollDirection, Int>,
	val mouseMovements: Map<Position, Int>,
) : Screen {

	@Composable
	override fun Content() {
		WindowInfo(
			keyPresses = keyPresses,
			bigrams = bigrams,
			trigrams = trigrams,
			mouseButtonPresses = mouseButtonPresses,
			scrollDirections = scrollDirections,
			mouseMovements = mouseMovements,
			focusTime = focusTime,
		)
	}
}

@Composable
private fun WindowInfo(
	keyPresses: Map<KC, Int>,
	bigrams: Map<Pair<KC, KC>, Int>,
	trigrams: Map<Triple<KC, KC, KC>, Int>,
	mouseButtonPresses: Map<MouseButton, Int>,
	scrollDirections: Map<ScrollDirection, Int>,
	mouseMovements: Map<Position, Int>,
	focusTime: Duration,
) {
	val keyPressesTotal = keyPresses.values.sum()

	val isHeatmapOpen = remember { mutableStateOf(false) }
	val isMousePositionsOpen = remember { mutableStateOf(false) }
	val isBigramsOpen = remember { mutableStateOf(false) }
	val isTrigramsOpen = remember { mutableStateOf(false) }

	// The content of the main window
	Column(
		modifier = Modifier.fillMaxSize()
	) {
		TopButtons(isHeatmapOpen, isMousePositionsOpen, isBigramsOpen, isTrigramsOpen)

		Row {
			Column(Modifier.fillMaxHeight().fillMaxWidth(0.5f)) {
				Table(
					columns = listOf(
						KeyColumnItem("keyboard.Key", 0.5f),
						KeyColumnItem("Count", 0.5f),
						KeyColumnItem("Percentage", 0.5f),
					),
					showScrollbar = true,
					data = keyPresses.entries.sortedByDescending { it.value }.map { (key, value) ->
						KeyRowItem(
							listOf(
								KeyColumnItem(key.unicode, 0.5f),
								KeyColumnItem(value.toString(), 0.5f),
								KeyColumnItem(
									"${
										String.format(
											"%.2f",
											(value.toDouble() / keyPressesTotal * 100)
										)
									}%",
									0.5f
								)
							)
						)
					}
				) {
					// The cells
					for (column in it.getColumns()) {
						TableCell(text = column.getName(), weight = column.getWeight())
					}
				}
			}
			Column {
				Table(
					columns = listOf(
						KeyColumnItem("MouseKey", 0.5f),
						KeyColumnItem("Count", 0.5f),
						KeyColumnItem("Percentage", 0.5f),
					),
					data = mouseButtonPresses.entries.sortedByDescending { it.value }
						.map { (key, value) ->
							KeyRowItem(
								listOf(
									KeyColumnItem(key.name.toPascalCase(), 0.5f),
									KeyColumnItem(value.toString(), 0.5f),
									KeyColumnItem(
										"${
											String.format(
												"%.2f",
												(value.toDouble() / mouseButtonPresses.values.sum() * 100)
											)
										}%",
										0.5f
									)
								)
							)
						}
				) {
					// The cells
					for (column in it.getColumns()) {
						TableCell(text = column.getName(), weight = column.getWeight())
					}
				}
				Table(
					columns = listOf(
						KeyColumnItem("ScrollDirection", 0.5f),
						KeyColumnItem("Count", 0.5f),
						KeyColumnItem("Percentage", 0.5f),
					),
					data = scrollDirections.entries.sortedByDescending { it.value }
						.map { (key, value) ->
							KeyRowItem(
								listOf(
									KeyColumnItem(key.name.toPascalCase(), 0.5f),
									KeyColumnItem(value.toString(), 0.5f),
									KeyColumnItem(
										"${
											String.format(
												"%.2f",
												(value.toDouble() / scrollDirections.values.sum() * 100)
											)
										}%",
										0.5f
									)
								)
							)
						}
				) {
					// The cells
					for (column in it.getColumns()) {
						TableCell(text = column.getName(), weight = column.getWeight())
					}
				}

				// Show the totals
				TotalsColumn(
					keyPresses = keyPresses,
					mouseButtonPresses = mouseButtonPresses,
					scrollDirections = scrollDirections,
					mouseMovements = mouseMovements,
					focusTime = focusTime
				)
			}
		}
	}

	// The heatmap window
	HeatmapWindow(
		isHeatmapWindowOpen = isHeatmapOpen,
		pressedKeys = keyPresses,
	)

	// The mouse positions window
	MousePositionsWindow(
		isMousePositionsOpen = isMousePositionsOpen,
		mousePositions = mouseMovements
	)

	// The bigrams window
	BigramsWindow(
		isBigramsOpen = isBigramsOpen,
		bigrams = bigrams
	)

	// The trigrams window
	TrigramsWindow(
		isTrigramsOpen = isTrigramsOpen,
		trigrams = trigrams
	)
}

@Composable
private fun TotalsColumn(
	keyPresses: Map<KC, Int>,
	mouseButtonPresses: Map<MouseButton, Int>,
	scrollDirections: Map<ScrollDirection, Int>,
	mouseMovements: Map<Position, Int>,
	focusTime: Duration,
) {
	val totals = remember {
		mutableStateOf(
			listOf(
				Total("KeyPresses", keyPresses.values.sum()),
				Total("MouseButtons", mouseButtonPresses.values.sum()),
				Total("Scrolls", scrollDirections.values.sum()),
				Total("MouseMovements", mouseMovements.values.sum())
			)
		)
	}

	Column(
		modifier = Modifier.fillMaxHeight(),
		verticalArrangement = Arrangement.Bottom
	) {
		Text("Totals", modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center)
		TotalsButtons(totals, focusTime)
		TotalsTables(totals)
	}
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun TotalsButtons(totals: MutableState<List<Total>>, focusTime: Duration) {
	val totalTimeUnit = TimeUnit("Total", 1.0)
	val selectedTimeUnit = remember { mutableStateOf(totalTimeUnit) }

	FlowRow(
		modifier = Modifier.fillMaxWidth(),
		horizontalArrangement = Arrangement.SpaceEvenly
	) {
		val timeUnits = listOf(
			totalTimeUnit,
			TimeUnit("Per Hour", focusTime.inWholeHours.toDouble()),
			TimeUnit("Per Minute", focusTime.inWholeMinutes.toDouble()),
			TimeUnit("Per Second", focusTime.inWholeSeconds.toDouble())
		)

		timeUnits.forEach { timeUnit ->
			Button(
				onClick = {
					totals.value = totals.value.map { total ->
						total.copy(value = getFirstTwoNonZeroDecimalValues(total.initialValue / timeUnit.divisor))
					}

					selectedTimeUnit.value = timeUnit
				},
				colors = if (selectedTimeUnit.value == timeUnit) {
					ButtonDefaults.buttonColors(backgroundColor = MaterialTheme.colors.secondary)
				} else {
					ButtonDefaults.buttonColors()
				}
			) {
				Text(timeUnit.label)
			}
		}
	}
}

@Composable
private fun TotalsTables(totals: MutableState<List<Total>>) {
	totals.value.chunked(2).forEach { chunk ->
		Table(
			columns = chunk.map { KeyColumnItem(it.label, 0.5f) },
			data = listOf(KeyRowItem(chunk.map { KeyColumnItem(it.value, 0.5f) }))
		) {
			for (column in it.getColumns()) {
				TableCell(text = column.getName(), weight = column.getWeight())
			}
		}
	}
}

private data class Total(
	val label: String,
	val initialValue: Int,
	var value: String = initialValue.toString(),
)

private data class TimeUnit(val label: String, val divisor: Double)

@Composable
private fun TopButtons(
	isHeatmapWindowOpen: MutableState<Boolean>,
	isMousePositionsWindowOpen: MutableState<Boolean>,
	isBigramsOpen: MutableState<Boolean>,
	isTrigramsOpen: MutableState<Boolean>,
) {
	Row(
		modifier = Modifier.fillMaxWidth()
			.padding(8.dp),
		horizontalArrangement = Arrangement.SpaceEvenly
	) {
		// Open the heatmap
		Button(
			onClick = {
				isHeatmapWindowOpen.value = true
			}
		) {
			Text("Open heatmap")
		}

		Button(
			onClick = {
				isMousePositionsWindowOpen.value = true
			}
		) {
			Text("Open mouse positions")
		}

		Button(
			onClick = {
				isBigramsOpen.value = true
			}
		) {
			Text("Open bigrams")
		}

		Button(
			onClick = {
				isTrigramsOpen.value = true
			}
		) {
			Text("Open trigrams")
		}
	}
}

private fun String.toPascalCase() = this.lowercase().replaceFirstChar { it.uppercase() }

// Return a string with the value up to the first two non-zero decimal values
private fun getFirstTwoNonZeroDecimalValues(number: Double): String {
	if (number % 1 == 0.0) return number.toInt().toString()
	val numberString = number.toString()
	val decimalIndex = numberString.indexOf('.')
	if (decimalIndex == -1) return numberString
	val decimalValues = numberString.substring(decimalIndex + 1)
	val firstNonZeroIndex = decimalValues.indexOfFirst { it != '0' }
	if (firstNonZeroIndex == -1) return numberString
	val secondNonZeroIndex = decimalValues.indexOfFirst { it != '0' && it != '.' }
	if (secondNonZeroIndex == -1) return numberString
	return numberString.substring(0, decimalIndex + 1 + secondNonZeroIndex + 1)
}