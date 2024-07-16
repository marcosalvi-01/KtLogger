package ui.heatmap

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Card
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import keyboard.AbstractKeyLayer
import keyboard.KC
import keyboard.LayerKey

data class ChangeKeyDialog(
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
			modifier = Modifier
				.fillMaxWidth()
				.padding(16.dp),
			shape = RoundedCornerShape(16.dp),
			backgroundColor = MaterialTheme.colors.surface,
			elevation = 0.dp
		) {
			Column(
				modifier = Modifier
					.padding(24.dp)
					.fillMaxWidth(),
				verticalArrangement = Arrangement.spacedBy(16.dp)
			) {
				Text(
					text = "Change Key",
					style = MaterialTheme.typography.h6,
					color = MaterialTheme.colors.onSurface
				)
				
				val options = remember { KC.entries }
				var expanded by remember { mutableStateOf(false) }
				var selectedOption by remember { mutableStateOf(options.firstOrNull()) }
				
				ExposedDropdownMenuBox(
					expanded = expanded,
					onExpandedChange = { expanded = !expanded }
				) {
					OutlinedTextField(
						value = selectedOption?.name ?: "",
						onValueChange = {},
						readOnly = true,
						label = { Text("Select Key") },
						trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
						modifier = Modifier.fillMaxWidth(),
						colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors()
					)
					
					ExposedDropdownMenu(
						expanded = expanded,
						onDismissRequest = { expanded = false },
						modifier = Modifier
							.verticalScroll(rememberScrollState())
							.heightIn(max = 300.dp)
					) {
						options.forEach { option ->
							DropdownMenuItem(
								onClick = {
									selectedOption = option
									expanded = false
								}
							) {
								Text(option.name)
							}
						}
					}
				}
				
				Row(
					modifier = Modifier.fillMaxWidth(),
					horizontalArrangement = Arrangement.End
				) {
					OutlinedButton(
						onClick = { state.onConfirm(state.row, state.col, LayerKey(KC.EMPTY)) },
						colors = ButtonDefaults.outlinedButtonColors(
							contentColor = MaterialTheme.colors.onSurface.copy(alpha = 0.6f)
						)
					) {
						Text("Clear key")
					}
					
					Spacer(modifier = Modifier.weight(1f))
					
					TextButton(
						onClick = state.onDismiss,
						colors = ButtonDefaults.textButtonColors(
							contentColor = MaterialTheme.colors.onSurface.copy(alpha = 0.6f)
						)
					) {
						Text("Cancel")
					}
					Spacer(modifier = Modifier.width(8.dp))
					Button(
						onClick = {
							selectedOption?.let {
								state.onConfirm(
									state.row,
									state.col,
									LayerKey(it)
								)
							}
						},
						enabled = selectedOption != null,
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