package ui.heatmap

import DropdownMenuNoPaddingVeitical
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.VerticalScrollbar
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.rememberScrollbarAdapter
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.AlertDialog
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Divider
import androidx.compose.material.DropdownMenuItem
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
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
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import database.Database
import keyboard.AbstractKeyLayer
import keyboard.AbstractKeymap
import keyboard.KC
import keyboard.Keymap
import keyboard.SplitKeymap

@Composable
fun HeatmapWindow(
	pressedKeys: Map<KC, Int>,
) {
	val areYouSureDialogState = remember { mutableStateOf<AreYouSureDialog?>(null) }
	val newKeymapDialogState = remember { mutableStateOf<NewKeymapDialog?>(null) }
	val newLayerDialogState = remember { mutableStateOf<NewLayerDialog?>(null) }
	val changeKeyDialogState = remember { mutableStateOf<ChangeKeyDialog?>(null) }
	
	Box {
		HeatmapBody(
			pressedKeys,
			areYouSureDialogState,
			newKeymapDialogState,
			newLayerDialogState,
			changeKeyDialogState
		)
		// Show a dialog to delete the layer
		areYouSureDialogState.value?.let {
			AreYouSureDialog(it)
		}
		
		// Show a dialog to create a new keymap
		newKeymapDialogState.value?.let {
			NewKeymapDialog(it)
		}
		
		// Show a dialog to create a new layer
		newLayerDialogState.value?.let {
			NewLayerDialog(it)
		}
		
		// Show a dialog to change the key
		changeKeyDialogState.value?.let {
			ChangeKeyDialog(it)
		}
	}
}

@Composable
fun HeatmapBody(
	pressedKeys: Map<KC, Int>,
	areYouSureDialogState: MutableState<AreYouSureDialog?>,
	newKeymapDialogState: MutableState<NewKeymapDialog?>,
	newLayerDialogState: MutableState<NewLayerDialog?>,
	changeKeyDialogState: MutableState<ChangeKeyDialog?>,
) {
	val scrollState = rememberScrollState()
	var keymaps by remember { mutableStateOf(Database.getKeymaps()) }
	val selectedKeymap = remember { mutableStateOf(keymaps.firstOrNull()) }
	
	Box {
		Column(
			modifier = Modifier.fillMaxSize().verticalScroll(scrollState)
				.padding(top = 10.dp, bottom = 15.dp, end = 15.dp)
		) {
			Row(
				modifier = Modifier.padding(end = 15.dp)
			) {
				Text(
					text = selectedKeymap.value?.name ?: "",
					style = MaterialTheme.typography.h6,
					modifier = Modifier.padding(start = 20.dp).align(Alignment.CenterVertically)
				)
				
				Spacer(modifier = Modifier.weight(1f))
				
				Box {
					KeymapSelector(
						keymaps = keymaps,
						onKeymapSelected = { selectedKeymap.value = it },
						onKeymapDeleted = {
							Database.deleteKeymap(it.name)
							keymaps = Database.getKeymaps()
							// If the selected keymap was deleted, select the first keymap
							if (selectedKeymap.value == it) selectedKeymap.value =
								keymaps.firstOrNull()
						},
						onNewKeymapCreated = {
							Database.createKeymap(it)
							keymaps = Database.getKeymaps()
							selectedKeymap.value = it
						},
						areYouSureDialogState = areYouSureDialogState,
						newKeymapDialogState = newKeymapDialogState
					)
				}
			}
			
			Divider(
				color = MaterialTheme.colors.onSurface.copy(alpha = 0.1f),
				modifier = Modifier.padding(start = 15.dp)
			)
			
			KeyboardCanvas(
				selectedKeymap,
				pressedKeys,
				areYouSureDialogState,
				newLayerDialogState,
				changeKeyDialogState
			)
		}
		VerticalScrollbar(
			modifier = Modifier.align(Alignment.CenterEnd).fillMaxHeight(),
			adapter = rememberScrollbarAdapter(scrollState)
		)
	}
}

@Composable
fun KeymapSelector(
	keymaps: List<AbstractKeymap>,
	onKeymapSelected: (AbstractKeymap) -> Unit,
	onKeymapDeleted: (AbstractKeymap) -> Unit,
	onNewKeymapCreated: (AbstractKeymap) -> Unit,
	areYouSureDialogState: MutableState<AreYouSureDialog?>,
	newKeymapDialogState: MutableState<NewKeymapDialog?>,
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
		modifier = Modifier.width(250.dp).background(MaterialTheme.colors.surface).padding(0.dp)
	) {
		keymaps.forEach { keymap ->
			DropdownMenuItem(onClick = {
				onKeymapSelected(keymap)
				showKeymapsDropdown = false
			}) {
				// Display the keymap name
				Text(
					text = keymap.name, modifier = Modifier.weight(1f)
				)
				
				Spacer(modifier = Modifier.width(8.dp))
				
				// Delete button
				IconButton(
					onClick = {
						areYouSureDialogState.value = AreYouSureDialog(title = "Delete keymap",
							text = "Are you sure you want to delete the keymap?",
							isDestructive = true,
							onYes = {
								onKeymapDeleted(keymap)
								areYouSureDialogState.value = null
							},
							onNo = { areYouSureDialogState.value = null })
					}, modifier = Modifier.size(32.dp)
				) {
					Icon(
						imageVector = Icons.Filled.Delete,
						contentDescription = "Delete",
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
		DropdownMenuItem(onClick = {
			newKeymapDialogState.value =
				NewKeymapDialog(onNewKeymapCreated = { name, rows, cols, thumbs ->
					onNewKeymapCreated(
						if (thumbs == null)
							Keymap(name = name, rows = rows, cols = cols)
						else
							SplitKeymap(
								name = name,
								sideRows = rows,
								sideCols = cols,
								thumbs = thumbs
							)
					)
				},
					onDismiss = { newKeymapDialogState.value = null },
					keymapNames = keymaps.map { it.name })
			showKeymapsDropdown = false
		}) {
			Row(
				modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically
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
	keymap: MutableState<AbstractKeymap?>,
	pressedKeys: Map<KC, Int>,
	areYouSureDialogState: MutableState<AreYouSureDialog?>,
	newLayerDialogState: MutableState<NewLayerDialog?>,
	changeKeyDialogState: MutableState<ChangeKeyDialog?>,
) {
	if (keymap.value == null) return
	
	// Create a TextMeasurer
	val textMeasurer = rememberTextMeasurer()
	
	Box(Modifier.fillMaxSize()) {
		Column(modifier = Modifier.fillMaxWidth()) {
			for (layer in keymap.value!!.layers) {
				Row(
					modifier = Modifier.fillMaxWidth(),
					verticalAlignment = Alignment.CenterVertically
				) {
					// The title of the layer
					Text(
						text = layer.name,
						style = MaterialTheme.typography.h5,
						modifier = Modifier.padding(start = 20.dp, top = 10.dp)
					)
					
					Spacer(modifier = Modifier.weight(1f))
					
					// Delete the layer
					IconButton(onClick = {
						areYouSureDialogState.value = AreYouSureDialog(title = "Delete layer",
							text = "Are you sure you want to delete the layer?",
							isDestructive = true,
							onYes = {
								keymap.value!!.removeLayer(layer.name)
								Database.updateKeymap(keymap.value!!)
								areYouSureDialogState.value = null
							},
							onNo = { areYouSureDialogState.value = null })
					}) {
						Icon(
							imageVector = Icons.Filled.Delete,
							contentDescription = "Delete",
							tint = MaterialTheme.colors.onBackground
						)
					}
				}
				
				// The keys in the layer
				val layerPressedKeys = pressedKeys.filterKeys { layer.contains(it) }
				val hoveredKey = remember { mutableStateOf<KC?>(null) }
				KeyLayer(
					layer,
					layerPressedKeys,
					// Calculate the height of the layer based on the number of rows
					(50 * keymap.value!!.rows).dp,
					MaterialTheme.colors.onBackground,
					textMeasurer,
					changeKeyDialogState,
					keymap.value!!,
					hoveredKey
				)
				
				// If the keymap is split and the thumbs are more than the columns - 2 or the keymap is not split
				if ((keymap.value is SplitKeymap && (keymap.value as SplitKeymap).thumbs > ((keymap.value as SplitKeymap).sideCols - 2)) ||
					keymap.value is Keymap
				) {
					val keyTotals = pressedKeys.values.sum()
					val onBackgroundColor = MaterialTheme.colors.onBackground
					Canvas(
						modifier = Modifier.fillMaxWidth().height(60.dp)
							.padding(bottom = 10.dp),
					) {
						hoveredKey.value?.let {
							drawKeyInfo(
								layer,
								pressedKeys,
								keyTotals,
								onBackgroundColor,
								textMeasurer,
								(size.width - (layer.cols + 1) * keySpacing.value) / layer.cols,
								(size.height - (layer.rows + 1) * keySpacing.value) / layer.rows,
								it,
								x = 15.dp.toPx(),
								y = (size.height - 10.dp.toPx()) / 2
							)
						}
						
						// Draw the palette only if the keymap is not split
						if (keymap.value is Keymap) {
							palette(
								size.width * 0.5f,
								size.height * 0.5f,
								Size(
									width = size.width * 0.4f,
									height = size.height * 0.7f
								),
								Direction.HORIZONTAL
							)
						}
					}
				}
				
				Divider(
					color = MaterialTheme.colors.onSurface.copy(alpha = 0.1f),
					modifier = Modifier.padding(start = 15.dp)
				)
			}
			
			Spacer(modifier = Modifier.height(20.dp))
			
			Box(
				modifier = Modifier.align(Alignment.CenterHorizontally)
					.background(color = MaterialTheme.colors.primary, shape = CircleShape)
					.size(48.dp), contentAlignment = Alignment.Center
			) {
				IconButton(onClick = {
					newLayerDialogState.value = NewLayerDialog(
						keymap = keymap.value!!,
						onDismiss = { newLayerDialogState.value = null },
						onConfirm = { name ->
							// Create the layer
							keymap.value!!.addLayer(name)
							// Update the keymap in the database
							Database.updateKeymap(keymap.value!!)
							// Close the dialog
							newLayerDialogState.value = null
						}
					)
				}) {
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

internal val keySpacing = 10.dp

@OptIn(ExperimentalComposeUiApi::class)
@Composable
private fun KeyLayer(
	layer: AbstractKeyLayer,
	pressedKeys: Map<KC, Int>,
	height: Dp,
	textColor: Color,
	textMeasurer: TextMeasurer,
	changeKeyDialogState: MutableState<ChangeKeyDialog?>,
	keymap: AbstractKeymap,
	hoveredKey: MutableState<KC?>,
) {
	val mousePosition = remember { mutableStateOf(Offset.Zero) }
	
	val keyTotals = pressedKeys.values.sum()
	
	Canvas(modifier = Modifier
		.height(height)
		.fillMaxWidth()
		.padding(10.dp)
		.onPointerEvent(PointerEventType.Move) {
			mousePosition.value = it.changes.first().position
			hoveredKey.value = findHoveredKey(
				mousePosition.value,
				layer,
				Size(size.width.toFloat(), size.height.toFloat()),
				this
			)
		}
		.pointerInput(layer) {
			detectTapGestures { offset ->
				// Find the clicked key
				val clickedKey = findClickedKey(
					offset,
					layer.apply { println("click layer: " + this.name) },
					Size(size.width.toFloat(), size.height.toFloat()),
					this
				)
				clickedKey?.let {
					changeKeyDialogState.value = ChangeKeyDialog(
						currentKey = layer.getKey(it.first, it.second)!!,
						keyLayer = layer,
						row = it.first,
						col = it.second,
						onDismiss = { changeKeyDialogState.value = null },
						onConfirm = { row, col, layerKey ->
							layer.setKey(row, col, layerKey)
							Database.updateKeymap(keymap)
							changeKeyDialogState.value = null
						}
					)
				}
			}
		}
	) {
		// Calculate the size of a key considering the spacing and the layer size
		val keyWidth = (size.width - (layer.cols + 1) * keySpacing.value) / layer.cols
		val keyHeight = (size.height - (layer.rows + 1) * keySpacing.value) / layer.rows
		
		// Draw the layer
		for (j in 0 until layer.rows) for (i in 0 until layer.cols) {
			val kc = layer.getKey(j, i)?.kc ?: continue
			
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
		
		// If the keymap is split draw the palette vertically in the middle
		if (keymap is SplitKeymap) {
			palette(
				size.width * 0.5f,
				size.height * 0.5f,
				Size(width = size.width * 0.04f, height = size.height - keySpacing.value * 2),
				Direction.VERTICAL
			)
		}
		
		// Display the number of times the hovered key was pressed for split keyboards
		if (keymap is SplitKeymap && keymap.thumbs <= (keymap.sideCols - 2)) {
			hoveredKey.value?.let {
				drawKeyInfo(
					layer,
					pressedKeys,
					keyTotals,
					textColor,
					textMeasurer,
					keyWidth,
					keyHeight,
					it
				)
			}
		}
	}
}

private fun DrawScope.drawKeyInfo(
	layer: AbstractKeyLayer,
	pressedKeys: Map<KC, Int>,
	keyTotals: Int,
	textColor: Color,
	textMeasurer: TextMeasurer,
	keyWidth: Float,
	keyHeight: Float,
	hoveredKey: KC?,
	x: Float? = null,
	y: Float? = null,
) {
	// Calculate the size of the text
	val text = "${pressedKeys.getOrDefault(hoveredKey, 0)}"
	val percentageText = "${
		String.format(
			"%.2f", pressedKeys.getOrDefault(hoveredKey, 0) / keyTotals.toFloat() * 100
		)
	}%"
	val textSize = textMeasurer.measure(text).size
	val percentageTextSize = textMeasurer.measure(percentageText).size
	
	// Calculate the position of the bottom left key
	val bottomLeftKeyX = x ?: (0 * keyWidth + (0 + 1) * keySpacing.value)
	val bottomLeftKeyY = y ?: ((layer.rows - 1) * keyHeight + (layer.rows) * keySpacing.value)
	
	// Calculate the x and y coordinates for the text
	val textX = bottomLeftKeyX + keyWidth / 2 - textSize.width / 2
	val textY = bottomLeftKeyY + keyHeight / 2 - textSize.height / 2
	
	// Calculate the x and y coordinates for the percentage text
	val percentageTextX =
		(bottomLeftKeyX + keyWidth) + keyWidth / 2 - percentageTextSize.width / 2
	val percentageTextY = bottomLeftKeyY + keyHeight / 2 - percentageTextSize.height / 2
	
	drawText(
		text = text, textMeasurer = textMeasurer, style = TextStyle(
			color = textColor,
			fontSize = 20.sp,
		),
		// Draw the text at the center of the bottom left key
		topLeft = Offset(x = textX, y = textY)
	)
	
	drawText(
		text = percentageText, textMeasurer = textMeasurer, style = TextStyle(
			color = textColor,
			fontSize = 20.sp,
		),
		// Draw the text at the center of the bottom left key
		topLeft = Offset(x = percentageTextX, y = percentageTextY)
	)
}

private fun findHoveredKey(
	position: Offset,
	layer: AbstractKeyLayer,
	size: Size,
	density: Density,
): KC? {
	with(density) {
		// Calculate the size of a key considering the spacing and the layer size
		val keyWidth = (size.width - (layer.cols + 1) * keySpacing.toPx()) / layer.cols
		val keyHeight = (size.height - (layer.rows + 1) * keySpacing.toPx()) / layer.rows
		
		// Iterate through all keys in the layer
		for (row in 0 until layer.rows) {
			for (col in 0 until layer.cols) {
				// Calculate the position of the key
				val x = col * keyWidth + (col + 1) * keySpacing.toPx()
				val y = row * keyHeight + (row + 1) * keySpacing.toPx()
				
				// Check if the position is within this key's bounds
				if (position.x >= x && position.x < x + keyWidth &&
					position.y >= y && position.y < y + keyHeight
				) {
					// Return the KC of the hovered key
					return layer.getKey(row, col)?.kc
				}
			}
		}
	}
	
	// If no key is hovered, return null
	return null
}

private fun findClickedKey(
	offset: Offset,
	layer: AbstractKeyLayer,
	canvasSize: Size,
	density: Density,
): Pair<Int, Int>? {
	with(density) {
		val keySpacing = 10.dp.toPx()
		val keyWidth = (canvasSize.width - (layer.cols + 1) * keySpacing) / layer.cols
		val keyHeight = (canvasSize.height - (layer.rows + 1) * keySpacing) / layer.rows
		
		for (j in 0 until layer.rows) {
			for (i in 0 until layer.cols) {
				val x = i * keyWidth + (i + 1) * keySpacing
				val y = j * keyHeight + (j + 1) * keySpacing
				val keyRect = Rect(
					left = x,
					top = y,
					right = x + keyWidth,
					bottom = y + keyHeight
				)
				if (keyRect.contains(offset)) {
					layer.getKey(j, i)?.kc?.let { return Pair(j, i) }
				}
			}
		}
	}
	return null
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
	
	val keyRect = Rect(
		left = x, top = y, right = x + keySize.width, bottom = y + keySize.height
	)
	
	// Check if the mouse is hovering over the key
	if (keyRect.contains(mousePosition.value)) hoveredKey.value = kc
	
	translate(
		left = x, top = y
	) {
		// Draw a rectangle for the key
		drawRoundRect(
			color = Color.LightGray, size = keySize, alpha = 0.8f,
			// Add a color filter to the key based on the number of times it was pressed
			colorFilter = ColorFilter.tint(
				interpolateColor(presses.toFloat() / maxPresses)
			), cornerRadius = CornerRadius(x = 10f, y = 10f)
		)
		
		// Calculate the offset based on the size of the key and the text
		val textSize = textMeasurer.measure(kc.unicode).size
		val offset = Offset(
			x = (keySize.width - textSize.width) / 2, y = (keySize.height - textSize.height) / 2
		)
		
		// Draw the key text on the key
		// centered horizontally and vertically
		drawText(
			textMeasurer = textMeasurer, text = kc.unicode, topLeft = offset, style = TextStyle(
				fontFamily = FontFamily("JetBrains Mono")
			)
		)
	}
}

internal enum class Direction {
	HORIZONTAL, VERTICAL
}

private fun DrawScope.palette(
	x: Float,
	y: Float,
	size: Size,
	direction: Direction = Direction.VERTICAL,
) {
	val steps = if (direction == Direction.VERTICAL) size.height.toInt() else size.width.toInt()
	translate(
		left = x - size.width / 2, top = y - size.height / 2
	) {
		for (i in 0..steps) {
			val color = interpolateColor(i / steps.toFloat())
			when (direction) {
				Direction.VERTICAL -> {
					drawRect(
						color = color,
						size = Size(width = size.width, height = size.height / steps),
						topLeft = Offset(x = 0f, y = (steps - i) * size.height / steps)
					)
				}
				
				Direction.HORIZONTAL -> {
					drawRect(
						color = color,
						size = Size(width = size.width / steps, height = size.height),
						topLeft = Offset(x = i * size.width / steps, y = 0f)
					)
				}
			}
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

@Composable
fun AreYouSureDialog(state: AreYouSureDialog) {
	AlertDialog(onDismissRequest = state.onNo,
		title = {
			Text(
				text = state.title,
				style = MaterialTheme.typography.h6,
				fontWeight = FontWeight.Bold
			)
		},
		text = {
			Text(
				text = state.text, style = MaterialTheme.typography.body1
			)
		},
		shape = RoundedCornerShape(16.dp),
		backgroundColor = MaterialTheme.colors.surface,
		contentColor = MaterialTheme.colors.onSurface,
		buttons = {
			Row(
				modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
				horizontalArrangement = Arrangement.End
			) {
				TextButton(
					onClick = state.onNo, colors = ButtonDefaults.textButtonColors(
						contentColor = MaterialTheme.colors.onSurface.copy(alpha = 0.6f)
					)
				) {
					Text(
						text = state.dismissButtonText, fontWeight = FontWeight.Medium
					)
				}
				Spacer(modifier = Modifier.width(8.dp))
				Button(
					onClick = state.onYes, colors = ButtonDefaults.buttonColors(
						backgroundColor = if (state.isDestructive) MaterialTheme.colors.error else MaterialTheme.colors.primary,
						contentColor = Color.White
					)
				) {
					Text(
						text = state.confirmButtonText, fontWeight = FontWeight.Bold
					)
				}
			}
		})
}

data class AreYouSureDialog(
	val title: String,
	val text: String,
	val isDestructive: Boolean = false,
	val confirmButtonText: String = "Yes",
	val dismissButtonText: String = "No",
	var onYes: () -> Unit,
	var onNo: () -> Unit,
)

data class NewKeymapDialog(
	val keymapNames: List<String>,
	val onNewKeymapCreated: (name: String, rows: Int, cols: Int, thumbs: Int?) -> Unit,
	val onDismiss: () -> Unit,
)