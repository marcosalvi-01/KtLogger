package ui.heatmap

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.Button
import androidx.compose.material.Divider
import androidx.compose.material.MaterialTheme
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Text
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
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.onPointerEvent
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import keyboard.AbstractKeyLayer
import keyboard.AbstractKeymap
import keyboard.KC

@Composable
fun NewLayerWindow(
	// Original state for the currently selected keymap
	originalKeymap: MutableState<AbstractKeymap?>,
) {
	// Copy the keymap to avoid modifying the original state
	val keymap = originalKeymap.value?.copy() ?: return
	Column {
		Header(keymap, originalKeymap)
		
		Divider(
			color = MaterialTheme.colors.onSurface.copy(alpha = 0.12f),
			modifier = Modifier.padding(horizontal = 15.dp),
		)
		
		val textMeasurer = rememberTextMeasurer()
		KeyLayer(
			layer = originalKeymap.value?.layers?.get(0) ?: return,
			height = 400.dp,
			textMeasurer = textMeasurer,
		)
	}
}

@Composable
fun Header(
	keymap: AbstractKeymap,
	originalKeymap: MutableState<AbstractKeymap?>,
) {
	val layerNames = keymap.layers.map { it.name }
	var name by remember { mutableStateOf("") }
	
	Row(
		modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 10.dp),
	) {
		// Name text field
		OutlinedTextField(
			value = name,
			onValueChange = { name = it },
			label = { Text("Name") },
			isError = name in layerNames,
			singleLine = true,
		)
		
		Spacer(Modifier.width(15.dp))
		
		// Error text
		if (name in layerNames) {
			Text(
				text = "Layer name already exists",
				color = MaterialTheme.colors.error,
				modifier = Modifier.align(Alignment.CenterVertically),
			)
		}
		
		Spacer(Modifier.weight(1f))
		
		// Confirm button
		Button(
			modifier = Modifier.align(Alignment.CenterVertically),
			onClick = {
				keymap.addLayer(name)
				originalKeymap.value = keymap
			},
			enabled = name.isNotBlank() && name !in layerNames,
		) {
			Text("Create")
		}
	}
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
private fun KeyLayer(
	layer: AbstractKeyLayer,
	height: Dp,
	textMeasurer: TextMeasurer,
) {
	val mousePosition = remember { mutableStateOf(Offset.Zero) }
	val hoveredKey = remember { mutableStateOf<KC?>(null) }
	
	val keySpacing = 10.dp
	
	Canvas(modifier = Modifier.height(height)
		.fillMaxWidth()
		.padding(10.dp)
		.onPointerEvent(PointerEventType.Move) {
			mousePosition.value = it.changes.first().position
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
			
			// Draw the key
			key(
				kc = kc,
				x = x,
				y = y,
				hoveredKey = hoveredKey,
				mousePosition = mousePosition,
				textMeasurer = textMeasurer,
				keySize = Size(width = keyWidth, height = keyHeight),
			)
		}
	}
}

@OptIn(ExperimentalTextApi::class)
private fun DrawScope.key(
	kc: KC,
	x: Float,
	y: Float,
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
			color = Color.LightGray,
			size = keySize,
			alpha = 0.8f,
			cornerRadius = CornerRadius(x = 10f, y = 10f)
		)
		
		// Calculate the offset based on the size of the key and the text
		val textSize = textMeasurer.measure(kc.unicode).size
		val offset = Offset(
			x = (keySize.width - textSize.width) / 2, y = (keySize.height - textSize.height) / 2
		)
		
		// Draw the key text on the key
		// centered horizontally and vertically
		drawText(
			textMeasurer = textMeasurer,
			text = kc.unicode,
			topLeft = offset,
		)
	}
}
