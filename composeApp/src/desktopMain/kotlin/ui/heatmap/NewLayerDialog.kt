package ui.heatmap

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Card
import androidx.compose.material.MaterialTheme
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import keyboard.AbstractKeymap

data class NewLayerDialog(
	val keymap: AbstractKeymap,
	val onDismiss: () -> Unit,
	val onConfirm: (name: String) -> Unit,
)

@Composable
fun NewLayerDialog(
	state: NewLayerDialog,
) {
	var name by remember { mutableStateOf("") }
	val isInputValid = name !in state.keymap.layers.map { it.name }
	
	Dialog(onDismissRequest = state.onDismiss) {
		Card(
			modifier = Modifier.fillMaxWidth().padding(16.dp), shape = RoundedCornerShape(16.dp),
			backgroundColor = MaterialTheme.colors.surface,
			contentColor = MaterialTheme.colors.onSurface,
			elevation = 0.dp // Needed to have the color of the card correct
		) {
			Column(
				modifier = Modifier.padding(16.dp),
				verticalArrangement = Arrangement.spacedBy(16.dp)
			) {
				DialogHeader("Create a new layer")
				
				OutlinedTextField(
					value = name,
					onValueChange = { name = it },
					label = { Text("Name") },
					singleLine = true,
				)
				
				Row(
					modifier = Modifier.align(Alignment.End),
					horizontalArrangement = Arrangement.spacedBy(10.dp),
				) {
					// Error message
					if (!isInputValid) {
						Text(
							modifier = Modifier.align(Alignment.CenterVertically),
							text = "Layer with this name already exists",
							style = MaterialTheme.typography.body2,
							color = MaterialTheme.colors.error,
						)
					}
					
					Spacer(modifier = Modifier.weight(1f))
					
					TextButton(
						onClick = state.onDismiss,
						colors = ButtonDefaults.textButtonColors(
							contentColor = MaterialTheme.colors.onSurface.copy(
								alpha = 0.6f
							)
						)
					) {
						Text("Cancel")
					}
					
					Button(
						onClick = { state.onConfirm(name) },
						enabled = name.isNotBlank() && isInputValid,
					) {
						Text("Create")
					}
				}
			}
		}
	}
}