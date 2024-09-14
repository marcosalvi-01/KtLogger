package ui.heatmap

import DropdownMenuNoPaddingVeitical
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Card
import androidx.compose.material.Divider
import androidx.compose.material.DropdownMenuItem
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.ExposedDropdownMenuBox
import androidx.compose.material.ExposedDropdownMenuDefaults
import androidx.compose.material.MaterialTheme
import androidx.compose.material.OutlinedButton
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.PopupProperties
import keyboard.AbstractKeyLayer
import keyboard.KC
import keyboard.LayerKey
import logger.windows.WindowsKeyPressesLogger.dataFlow

//import logger.KeyboardLogger.keyPresses


data class ChangeKeyDialog(
	val currentKey: LayerKey,
	val keyLayer: AbstractKeyLayer,
	val row: Int,
	val col: Int,
	val onDismiss: () -> Unit,
	val onConfirm: (row: Int, col: Int, layerKey: LayerKey) -> Unit,
)

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun ChangeKeyDialog(state: ChangeKeyDialog) {
	Dialog(onDismissRequest = state.onDismiss) {
		Card(
			modifier = Modifier.fillMaxWidth().padding(16.dp),
			shape = RoundedCornerShape(16.dp),
			backgroundColor = MaterialTheme.colors.surface,
			elevation = 0.dp
		) {
			Column(
				modifier = Modifier.padding(24.dp).fillMaxWidth(),
				verticalArrangement = Arrangement.spacedBy(16.dp)
			) {
				Text(
					text = "Change Key",
					style = MaterialTheme.typography.h6,
					color = MaterialTheme.colors.onSurface
				)
				
				val options = remember { KC.entries.toList() }
				var expanded by remember { mutableStateOf(false) }
				var selectedOption by remember { mutableStateOf(state.currentKey.kc) }
				var textFieldValue by remember { mutableStateOf(state.currentKey.kc.toString()) }
				var filteredOptions by remember { mutableStateOf(options) }
				var isDetecting by remember { mutableStateOf(false) }
//				val pressedKey by keyPresses.collectAsState(KC.EMPTY)
				val pressedKey by dataFlow.collectAsState(null)
				
				if (isDetecting && pressedKey != null) {
					selectedOption = pressedKey!!.data.key
					textFieldValue = pressedKey!!.data.key.toString()
				}
				
				ExposedDropdownMenuBox(
					expanded = expanded,
					onExpandedChange = { expanded = !expanded }) {
					OutlinedTextField(
						value = textFieldValue,
						singleLine = true,
						onValueChange = {
							textFieldValue = it
							selectedOption = KC.entries.find { kc -> kc.name == it } ?: KC.EMPTY
							filteredOptions = options.filter { kc ->
								kc.metadata()?.contains(textFieldValue, ignoreCase = true) ?: false
							}
							expanded = true
						},
						label = { Text("Select or Type Key") },
						trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
						modifier = Modifier.fillMaxWidth(),
						colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
						// Disable the text field when detecting a key
						readOnly = isDetecting,
					)
					
					DropdownMenuNoPaddingVeitical(
						modifier = Modifier.exposedDropdownSize(true),
						properties = PopupProperties(focusable = false),
						// don't show the dropdown if detecting the key
						expanded = expanded && !isDetecting,
						onDismissRequest = { expanded = false },
					) {
						Row(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
							// Headers: Name, Symbol, Unicode
							DropdownCustomRow("Name", false)
							DropdownCustomRow("Symbol", false)
							DropdownCustomRow("Unicode", false)
						}
						Divider(
							color = MaterialTheme.colors.onSurface.copy(alpha = 0.5f),
							modifier = Modifier.padding(horizontal = 8.dp)
						)
						filteredOptions.forEach { option ->
							DropdownMenuItem(onClick = {
								selectedOption = option
								textFieldValue = option.name
								expanded = false
							}) {
								Row(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
									DropdownCustomRow(option.toString())
									DropdownCustomRow(option.symbol)
									DropdownCustomRow(option.unicode)
								}
							}
						}
					}
				}
				
				if (isDetecting) {
					Text(
						"Right now there are some issues with detecting modified keys (symbols like @, #, etc.), " +
								"please use the search dropdown to select those keys.",
						// Warning
						color = Color(0xffffc02c),
					)
				}
				
				Row(
					modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End
				) {
					OutlinedButton(
						onClick = { state.onConfirm(state.row, state.col, LayerKey(KC.EMPTY)) },
						colors = ButtonDefaults.outlinedButtonColors(
							contentColor = MaterialTheme.colors.onSurface.copy(alpha = 0.6f)
						)
					) {
						Text("Clear key")
					}
					
					Spacer(modifier = Modifier.padding(horizontal = 8.dp))
					
					// Detect key
					OutlinedButton(
						onClick = {
							isDetecting = !isDetecting
						},
						colors = if (!isDetecting) ButtonDefaults.buttonColors(
							backgroundColor = MaterialTheme.colors.primary,
							contentColor = MaterialTheme.colors.onPrimary
						) else ButtonDefaults.buttonColors(
							backgroundColor = MaterialTheme.colors.secondary,
							contentColor = MaterialTheme.colors.onSecondary
						)
					) {
						Text(if (!isDetecting) "Detect key" else "Detecting key...")
					}
					
					Spacer(modifier = Modifier.weight(1f))
					
					TextButton(
						onClick = state.onDismiss, colors = ButtonDefaults.textButtonColors(
							contentColor = MaterialTheme.colors.onSurface.copy(alpha = 0.6f)
						)
					) {
						Text("Cancel")
					}
					Spacer(modifier = Modifier.width(8.dp))
					Button(
						onClick = {
							state.onConfirm(
								state.row, state.col, LayerKey(selectedOption)
							)
						},
						enabled = selectedOption != KC.EMPTY,
						colors = ButtonDefaults.buttonColors(
							backgroundColor = MaterialTheme.colors.primary,
							contentColor = MaterialTheme.colors.onPrimary
						)
					) {
						Text("Confirm")
					}
				}
			}
		}
	}
}

@Composable
internal fun RowScope.DropdownCustomRow(
	text: String,
	background: Boolean = true,
) {
	val modifier = if (background) {
		Modifier.weight(1f)
			.align(Alignment.CenterVertically)
			.padding(horizontal = 8.dp)
			.background(MaterialTheme.colors.secondaryVariant, RoundedCornerShape(6.dp))
			.padding(top = 4.dp, bottom = 5.dp, start = 8.dp, end = 8.dp)
	} else {
		Modifier.weight(1f)
	}
	
	Text(
		text = text,
		modifier = modifier,
		textAlign = TextAlign.Center,
		color = MaterialTheme.colors.onSecondary
	)
}