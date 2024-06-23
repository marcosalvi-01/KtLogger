package ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.window.WindowDraggableArea
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.onPointerEvent
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.WindowState
import icon
import keyboard.KC
import keyboard.KeyLayer
import keyboard.MyKeymap

@Composable
fun HeatmapWindow(
	isHeatmapWindowOpen: MutableState<Boolean>,
	pressedKeys: Map<KC, Int>,
) {
	val windowState = WindowState(width = 1000.dp)

	Window(
		onCloseRequest = {
			isHeatmapWindowOpen.value = false
		},
		state = windowState,
		title = "Heatmap",
		icon = icon,
		visible = isHeatmapWindowOpen.value,
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
						title = { Text("Heatmap") },
						actions = {
							// Close the window
							IconButton(onClick = {
								isHeatmapWindowOpen.value = false
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

				Column(
					modifier = Modifier.fillMaxSize()
				) {
					// Display the heatmap
					KeyboardCanvas(MyKeymap.layers(), pressedKeys)
				}
			}
		}
	}
}

@Composable
private fun KeyboardCanvas(
	layers: List<KeyLayer>,
	pressedKeys: Map<KC, Int>,
) {
	// Create a scroll state
	val scrollState = rememberScrollState()
	// Create a TextMeasurer
	val textMeasurer = rememberTextMeasurer()

	// Show the layers in a lazy column
	Column(
		modifier = Modifier
			.verticalScroll(scrollState)  // Add verticalScroll modifier
	) {
		for (layer in layers) {
			val layerPressedKeys = pressedKeys.filterKeys { layer.contains(it) }
			KeyLayer(
				layer,
				layerPressedKeys,
				200.dp,
				MaterialTheme.colors.onBackground,
				textMeasurer
			)
		}
	}
}

@OptIn(ExperimentalComposeUiApi::class)
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
					fontSize = 20.sp
				),
				// Draw the text at the center of the bottom left key
				topLeft = Offset(x = textX, y = textY)
			)

			drawText(
				text = percentageText,
				textMeasurer = textMeasurer,
				style = TextStyle(
					color = textColor,
					fontSize = 20.sp
				),
				// Draw the text at the center of the bottom left key
				topLeft = Offset(x = percentageTextX, y = percentageTextY)
			)
		}
	}
}

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
		drawRect(
			color = Color.LightGray,
			size = keySize,
			alpha = 0.8f,
			// Add a color filter to the key based on the number of times it was pressed
			colorFilter = ColorFilter.tint(
				interpolateColor(presses.toFloat() / maxPresses)
			),
		)

		// Calculate the offset based on the size of the key and the text
		val textSize = textMeasurer.measure(kc.symbol).size
		val offset = Offset(
			x = (keySize.width - textSize.width) / 2,
			y = (keySize.height - textSize.height) / 2
		)

		// Draw the key text on the key
		// centered horizontally and vertically
		drawText(
			textMeasurer = textMeasurer,
			text = kc.symbol,
			topLeft = offset,
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
