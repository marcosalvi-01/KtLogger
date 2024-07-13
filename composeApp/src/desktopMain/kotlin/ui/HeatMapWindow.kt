package ui

import DropdownMenuNoPaddingVeitical
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.window.WindowDraggableArea
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.rounded.Close
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.onPointerEvent
import androidx.compose.ui.text.*
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.WindowState
import database.Database
import icon
import keyboard.*

@Composable
fun HeatmapWindow(
	isHeatmapWindowOpen: MutableState<Boolean>,
	pressedKeys: Map<KC, Int>,
) {
	val windowState = WindowState(width = 1000.dp)

	Window(
		onCloseRequest = { isHeatmapWindowOpen.value = false },
		state = windowState,
		title = "Heatmap",
		icon = icon,
		visible = isHeatmapWindowOpen.value,
		transparent = true,
		undecorated = true
	) {
		Surface(
			modifier = Modifier
				.fillMaxSize()
				.padding(5.dp)
				.shadow(3.dp, RoundedCornerShape(10.dp)),
			color = MaterialTheme.colors.background,
			shape = RoundedCornerShape(10.dp)
		) {
			Column(modifier = Modifier.fillMaxSize()) {
				WindowDraggableArea {
					TopAppBar(
						title = { Text("Heatmap") },
						actions = {
							IconButton(onClick = { isHeatmapWindowOpen.value = false }) {
								Icon(
									imageVector = Icons.Rounded.Close,
									contentDescription = "Close",
									tint = MaterialTheme.colors.onBackground
								)
							}
						}
					)
				}
				HeatmapBody(pressedKeys)
			}
		}
	}
}

@Composable
fun HeatmapBody(pressedKeys: Map<KC, Int>) {
	val scrollState = rememberScrollState()
	var keymaps by remember { mutableStateOf(Database.getKeymaps()) }
	var selectedKeymap by remember { mutableStateOf(keymaps.firstOrNull()) }

	Box {
		Column(
			modifier = Modifier
				.fillMaxSize()
				.verticalScroll(scrollState)
				.padding(top = 10.dp, bottom = 15.dp, end = 15.dp)
		) {
			Box(
				modifier = Modifier.align(Alignment.End).padding(end = 15.dp)
			) {
				KeymapSelector(
					keymaps = keymaps,
					onKeymapSelected = { selectedKeymap = it },
					onKeymapDeleted = {
						Database.deleteKeymap(it.name)
						keymaps = Database.getKeymaps()
						selectedKeymap = keymaps.firstOrNull()
					},
					onNewKeymapCreated = {
						Database.createKeymap(defaultKeymap.copy(name = "New keymap"))
						keymaps = Database.getKeymaps()
						selectedKeymap = keymaps.firstOrNull()
					}
				)
			}

			KeyboardCanvas(selectedKeymap, pressedKeys)
		}
		VerticalScrollbar(
			modifier = Modifier.align(Alignment.CenterEnd).fillMaxHeight(),
			adapter = rememberScrollbarAdapter(scrollState)
		)
	}
}

@Composable
fun KeymapSelector(
	keymaps: List<Keymap>,
	onKeymapSelected: (Keymap) -> Unit,
	onKeymapDeleted: (Keymap) -> Unit,
	onNewKeymapCreated: () -> Unit,
) {
	var showKeymapsDropdown by remember { mutableStateOf(false) }
	Button(
		onClick = { showKeymapsDropdown = true },
	) {
		Text("Keymaps")
	}
	DropdownMenuNoPaddingVeitical(
		expanded = showKeymapsDropdown,
		onDismissRequest = { showKeymapsDropdown = false },
		modifier = Modifier
			.width(250.dp)
			.background(MaterialTheme.colors.surface)
			.padding(0.dp)
	) {
		keymaps.forEach { keymap ->
			DropdownMenuItem(
				onClick = {
					onKeymapSelected(keymap)
					showKeymapsDropdown = false
				}
			) {
				Text(
					text = keymap.name,
					modifier = Modifier.weight(1f)
				)
				Spacer(modifier = Modifier.width(8.dp))
				IconButton(
					onClick = {
						onKeymapDeleted(keymap)
						showKeymapsDropdown = false
					},
					modifier = Modifier.size(32.dp)
				) {
					Icon(
						imageVector = Icons.Filled.Delete,
						contentDescription = "Delete",
						tint = MaterialTheme.colors.onSurface.copy(alpha = 0.7f),
						modifier = Modifier.size(20.dp)
					)
				}
				IconButton(
					onClick = {
						showKeymapsDropdown = false
						// TODO: Implement edit functionality
					},
					modifier = Modifier.size(32.dp)
				) {
					Icon(
						imageVector = Icons.Filled.Edit,
						contentDescription = "Edit",
						tint = MaterialTheme.colors.onSurface.copy(alpha = 0.7f),
						modifier = Modifier.size(20.dp)
					)
				}
			}
			if (keymap != keymaps.last()) {
				Divider(color = MaterialTheme.colors.onSurface.copy(alpha = 0.1f))
			}
		}
		Divider(color = MaterialTheme.colors.onSurface.copy(alpha = 0.2f), thickness = 1.dp)
		DropdownMenuItem(
			onClick = {
				onNewKeymapCreated()
				showKeymapsDropdown = false
			}
		) {
			Row(
				modifier = Modifier.fillMaxWidth(),
				verticalAlignment = Alignment.CenterVertically
			) {
				Text(
					"New keymap",
					style = MaterialTheme.typography.button.copy(fontWeight = FontWeight.Bold),
					color = MaterialTheme.colors.onPrimary,
				)
				Spacer(Modifier.weight(1f))
				Icon(
					imageVector = Icons.Filled.Add,
					contentDescription = "Create",
					tint = MaterialTheme.colors.onPrimary,
					modifier = Modifier.size(24.dp)
				)
			}
		}
	}
}

@Composable
private fun KeyboardCanvas(
	keymap: Keymap?,
	pressedKeys: Map<KC, Int>,
) {
	if (keymap == null) return

	// Create a TextMeasurer
	val textMeasurer = rememberTextMeasurer()

	Box(Modifier.fillMaxSize()) {
		// Show the layers in a lazy column
		Column(
			modifier = Modifier
		) {
			for (layer in keymap.layers) {
				Text(
					text = layer.name,
					style = MaterialTheme.typography.h5,
					modifier = Modifier.padding(start = 20.dp)
				)

				val layerPressedKeys = pressedKeys.filterKeys { layer.contains(it) }
				KeyLayer(
					layer,
					layerPressedKeys,
					200.dp,
					MaterialTheme.colors.onBackground,
					textMeasurer
				)
			}

			Box(
				modifier = Modifier
					.align(Alignment.CenterHorizontally)
					.background(color = MaterialTheme.colors.primary, shape = CircleShape)
					.size(48.dp), // Adjust size to control the overall size of the circle
				contentAlignment = Alignment.Center
			) {
				IconButton(
					onClick = {
						// Show a dialog to create a new layer
						TODO()
					}
				) {
					Icon(
						imageVector = Icons.Filled.Add,
						contentDescription = "Create",
						tint = MaterialTheme.colors.onBackground
					)
				}
			}
		}
	}
}

@OptIn(ExperimentalComposeUiApi::class, ExperimentalTextApi::class)
@Composable
private fun KeyLayer(
	layer: KeyLayer,
	pressedKeys: Map<KC, Int>,
	height: Dp,
	textColor: Color,
	textMeasurer: TextMeasurer,
) {
	val mousePosition = remember { mutableStateOf(Offset.Zero) }
	val hoveredKey = remember { mutableStateOf<KC?>(null) }

	val keySpacing = 10.dp

	val keyTotals = pressedKeys.values.sum()

	Canvas(
		modifier = Modifier.height(height)
			.fillMaxWidth()
			.padding(10.dp)
			.onPointerEvent(PointerEventType.Move) {
				mousePosition.value = it.changes.first().position
			}
	) {
		// Calculate the size of a key considering the spacing and the layer size
		val keyWidth = (size.width - (layer.getWidth() + 1) * keySpacing.value) / layer.getWidth()
		val keyHeight =
			(size.height - (layer.getHeight() + 1) * keySpacing.value) / layer.getHeight()

		// Draw the layer
		for (j in 0 until layer.getHeight())
			for (i in 0 until layer.getWidth()) {
				val kc = layer.getKc(j, i)

				// Calculate the position of the key considering the spacing and the layer size
				val x = i * keyWidth + (i + 1) * keySpacing.value
				val y = j * keyHeight + (j + 1) * keySpacing.value

				key(
					kc,
					pressedKeys.getOrDefault(kc, 0),
					x,
					y,
					pressedKeys.values.maxOrNull() ?: 1,
					hoveredKey,
					mousePosition,
					textMeasurer,
					Size(width = keyWidth, height = keyHeight)
				)
			}

		// Draw the color palette in the middle vertically
		palette(
			size.width * 0.5f,
			size.height * 0.5f,
			Size(width = size.width * 0.04f, height = size.height - keySpacing.value * 2),
		)

		// Display the number of times the hovered key was pressed
		hoveredKey.value?.let {
			// Calculate the size of the text
			val text = "${pressedKeys.getOrDefault(it, 0)}"
			val percentageText = "${
				String.format("%.2f", pressedKeys.getOrDefault(it, 0) / keyTotals.toFloat() * 100)
			}%"
			val textSize = textMeasurer.measure(text).size
			val percentageTextSize = textMeasurer.measure(percentageText).size

			// Calculate the position of the bottom left key
			val bottomLeftKeyX = 0 * keyWidth + (0 + 1) * keySpacing.value
			val bottomLeftKeyY =
				(layer.getHeight() - 1) * keyHeight + (layer.getHeight()) * keySpacing.value

			// Calculate the x and y coordinates for the text
			val textX = bottomLeftKeyX + keyWidth / 2 - textSize.width / 2
			val textY = bottomLeftKeyY + keyHeight / 2 - textSize.height / 2

			// Calculate the x and y coordinates for the percentage text
			val percentageTextX =
				(bottomLeftKeyX + keyWidth) + keyWidth / 2 - percentageTextSize.width / 2
			val percentageTextY = bottomLeftKeyY + keyHeight / 2 - percentageTextSize.height / 2

			drawText(
				text = text,
				textMeasurer = textMeasurer,
				style = TextStyle(
					color = textColor,
					fontSize = 20.sp,
					fontFamily = FontFamily("JetBrains Mono")
				),
				// Draw the text at the center of the bottom left key
				topLeft = Offset(x = textX, y = textY)
			)

			drawText(
				text = percentageText,
				textMeasurer = textMeasurer,
				style = TextStyle(
					color = textColor,
					fontSize = 20.sp,
					fontFamily = FontFamily("JetBrains Mono")
				),
				// Draw the text at the center of the bottom left key
				topLeft = Offset(x = percentageTextX, y = percentageTextY)
			)
		}
	}
}

@OptIn(ExperimentalTextApi::class)
private fun DrawScope.key(
	kc: KC,
	presses: Int,
	x: Float,
	y: Float,
	maxPresses: Int,  // The maximum number of presses for the keys in this layer
	hoveredKey: MutableState<KC?>,
	mousePosition: MutableState<Offset>,
	textMeasurer: TextMeasurer,
	keySize: Size,
) {
	if (kc == KC.UNKNOWN) return

//	val size = Size(width = layerSize.width * 0.08f, height = layerSize.height * 0.2f)

	val keyRect = Rect(
		left = x,
		top = y,
		right = x + keySize.width,
		bottom = y + keySize.height
	)

	// Check if the mouse is hovering over the key
	if (keyRect.contains(mousePosition.value))
		hoveredKey.value = kc

	translate(
		left = x,
		top = y
	) {
		// Draw a rectangle for the key
		drawRoundRect(
			color = Color.LightGray,
			size = keySize,
			alpha = 0.8f,
			// Add a color filter to the key based on the number of times it was pressed
			colorFilter = ColorFilter.tint(
				interpolateColor(presses.toFloat() / maxPresses)
			),
			cornerRadius = CornerRadius(x = 10f, y = 10f)
		)

		// Calculate the offset based on the size of the key and the text
		val textSize = textMeasurer.measure(kc.unicode).size
		val offset = Offset(
			x = (keySize.width - textSize.width) / 2,
			y = (keySize.height - textSize.height) / 2
		)

		// Draw the key text on the key
		// centered horizontally and vertically
		drawText(
			textMeasurer = textMeasurer,
			text = kc.unicode,
			topLeft = offset,
			style = TextStyle(
				fontFamily = FontFamily("JetBrains Mono")
			)
		)
	}
}

// Draw the color palette vertically using interpolateColor
private fun DrawScope.palette(x: Float, y: Float, size: Size) {
	val steps = size.height.toInt()
	translate(
		left = x - size.width / 2,
		top = y - size.height / 2
	) {
		for (i in 0..steps) {
			val color = interpolateColor((steps - i) / steps.toFloat())
			drawRect(
				color = color,
				size = Size(width = size.width, height = size.height / steps),
				topLeft = Offset(x = 0f, y = i * size.height / steps)
			)
		}
	}
}

/**
 * Interpolates between two colors based on a value between 0 and 1.
 */
fun interpolateColor(value: Float): Color {
	val palette = listOf(
		Color.White,
		Color.Red,
	)

	val clampedValue = value.coerceIn(0f, 1f)

	if (clampedValue <= 0f) return palette.first()
	if (clampedValue >= 1f) return palette.last()

	val scaledValue = clampedValue * (palette.size - 1)
	val index = scaledValue.toInt()
	val fraction = scaledValue - index

	return lerp(palette[index], palette[index + 1], fraction)
}
