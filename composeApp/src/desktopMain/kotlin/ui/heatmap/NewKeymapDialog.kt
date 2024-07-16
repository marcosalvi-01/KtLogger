package ui.heatmap

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Card
import androidx.compose.material.Checkbox
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog

@Composable
fun NewKeymapDialog(state: NewKeymapDialog) {
	var title by remember { mutableStateOf("") }
	var rows by remember { mutableStateOf("") }
	var cols by remember { mutableStateOf("") }
	var split by remember { mutableStateOf(false) }
	var thumbs by remember { mutableStateOf("") }
	
	val isInputValid = title.isNotBlank() &&
			(title !in state.keymapNames) &&
			(rows.toIntOrNull() != null) &&
			(cols.toIntOrNull() != null) &&
			(!split || (thumbs.toIntOrNull() != null)) &&
			(!split || (thumbs.toIntOrNull() ?: 0) <= (cols.toIntOrNull() ?: 0))
	
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
				DialogHeader("Create a new keymap")
				KeymapForm(state = state,
					title = title,
					onTitleChange = { title = it },
					rows = rows,
					onRowsChange = { rows = it },
					cols = cols,
					onColsChange = { cols = it },
					split = split,
					onSplitChange = { split = it },
					thumbs = thumbs,
					onThumbsChange = { thumbs = it })
				DialogActions(
					state = state,
					isInputValid = isInputValid,
					title = title,
					rows = rows,
					cols = cols,
					split = split,
					thumbs = thumbs
				)
			}
		}
	}
}

@Composable
fun DialogHeader(
	text: String,
) {
	Text(
		text = text,
		style = MaterialTheme.typography.h6,
		fontWeight = FontWeight.Bold
	)
}

@Composable
private fun KeymapForm(
	state: NewKeymapDialog,
	title: String,
	onTitleChange: (String) -> Unit,
	rows: String,
	onRowsChange: (String) -> Unit,
	cols: String,
	onColsChange: (String) -> Unit,
	split: Boolean,
	onSplitChange: (Boolean) -> Unit,
	thumbs: String,
	onThumbsChange: (String) -> Unit,
) {
	KeymapNameInput(title, onTitleChange)
	KeymapDimensionsInput(rows, cols, onRowsChange, onColsChange)
	SplitKeyboardOption(split, onSplitChange, thumbs, onThumbsChange)
	
	if (title in state.keymapNames) {
		Text(
			text = "A keymap with this name already exists",
			color = MaterialTheme.colors.error,
			modifier = Modifier.padding(top = 4.dp)
		)
	}
	
	// Thumb count must be greater than 0 and less than the number of columns
	if (split && (thumbs.toIntOrNull() ?: 0) > (cols.toIntOrNull() ?: Int.MAX_VALUE)) {
		Text(
			text = "The number of thumb keys must be between 0 and ${cols.toIntOrNull()}",
			color = MaterialTheme.colors.error,
			modifier = Modifier.padding(top = 4.dp)
		)
	}
}

@Composable
private fun KeymapNameInput(value: String, onValueChange: (String) -> Unit) {
	OutlinedTextField(
		value = value,
		onValueChange = onValueChange,
		label = { Text("Name") },
		modifier = Modifier.fillMaxWidth(),
		singleLine = true
	)
}

@Composable
private fun KeymapDimensionsInput(
	rows: String,
	cols: String,
	onRowsChange: (String) -> Unit,
	onColsChange: (String) -> Unit,
) {
	Row(
		modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)
	) {
		OutlinedTextField(
			value = rows,
			onValueChange = onRowsChange,
			label = { Text("Rows") },
			keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
			modifier = Modifier.weight(1f),
			singleLine = true
		)
		OutlinedTextField(
			value = cols,
			onValueChange = onColsChange,
			label = { Text("Columns") },
			keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
			modifier = Modifier.weight(1f),
			singleLine = true
		)
	}
}

@Composable
private fun SplitKeyboardOption(
	split: Boolean,
	onSplitChange: (Boolean) -> Unit,
	thumbs: String,
	onThumbsChange: (String) -> Unit,
) {
	Row(
		verticalAlignment = Alignment.CenterVertically,
	) {
		Checkbox(
			checked = split, onCheckedChange = onSplitChange
		)
		Text("Split Keyboard")
		if (split) {
			Spacer(modifier = Modifier.width(16.dp))
			OutlinedTextField(
				value = thumbs,
				onValueChange = onThumbsChange,
				label = { Text("Thumbs") },
				keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
				modifier = Modifier.width(100.dp),
				singleLine = true
			)
		}
	}
}

@Composable
private fun DialogActions(
	state: NewKeymapDialog,
	isInputValid: Boolean,
	title: String,
	rows: String,
	cols: String,
	split: Boolean,
	thumbs: String,
) {
	Row(
		modifier = Modifier.fillMaxWidth(),
		horizontalArrangement = Arrangement.End,
		verticalAlignment = Alignment.CenterVertically
	) {
		TextButton(
			onClick = state.onDismiss, colors = ButtonDefaults.textButtonColors(
				contentColor = MaterialTheme.colors.onSurface.copy(alpha = 0.6f)
			)
		) {
			Text(text = "Cancel", fontWeight = FontWeight.Medium)
		}
		Spacer(modifier = Modifier.width(8.dp))
		Button(
			onClick = {
				state.onNewKeymapCreated(
					title,
					rows.toIntOrNull() ?: 0,
					cols.toIntOrNull() ?: 0,
					if (split) thumbs.toIntOrNull() else null
				)
				state.onDismiss()
			}, enabled = isInputValid
		) {
			Text("Create")
		}
	}
}