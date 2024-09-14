package ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import keyboard.KC
import ui.heatmap.DialogHeader

data class FilterState(
	var minCount: Int = 0,
	var showNumbers: Boolean = true,
	var showSymbols: Boolean = true,
	var showNavigation: Boolean = true,
	var showLetters: Boolean = true,
	var showModifiers: Boolean = true,
)

@Composable
fun BigramsWindow(
	bigrams: Map<Pair<KC, KC>, Int>,
) {
	var showFilterDialog by remember { mutableStateOf(false) }
	var filterState by remember { mutableStateOf(FilterState()) }
	var bigramsToShow by remember { mutableStateOf(bigrams) }
	
	Box {
		BigramsBody(bigramsToShow, { showFilterDialog = true })
		
		if (showFilterDialog) {
			FilterDialog(
				bigrams = bigrams,
				initialFilterState = filterState,
				onDismiss = { showFilterDialog = false },
				onConfirm = { newFilterState, filteredBigrams ->
					filterState = newFilterState
					bigramsToShow = filteredBigrams
					showFilterDialog = false
				}
			)
		}
	}
}

@Composable
private fun BigramsBody(
	bigrams: Map<Pair<KC, KC>, Int>,
	onFilterClick: () -> Unit,
) {
	Column {
		Button(
			modifier = Modifier.align(Alignment.CenterHorizontally),
			onClick = onFilterClick
		) {
			Text("Filter")
		}
		
		Table(
			columns = listOf(
				KeyColumnItem("Bigram", 0.5f),
				KeyColumnItem("Count", 0.5f),
				KeyColumnItem("Percentage", 0.5f),
			),
			showScrollbar = true,
			data = bigrams.entries.sortedByDescending { it.value }
				.map { (bigram, value) ->
					KeyRowItem(
						listOf(
							KeyColumnItem(
								bigram.first.unicode + " " + bigram.second.unicode,
								0.5f
							),
							KeyColumnItem(value.toString(), 0.5f),
							KeyColumnItem(
								"${
									String.format(
										"%.2f",
										(value.toDouble() / bigrams.values.sum() * 100)
									)
								}%",
								0.5f
							)
						)
					)
				}
		) {
			for (column in it.getColumns()) {
				TableCell(text = column.getName(), weight = column.getWeight())
			}
		}
	}
}

@Composable
private fun FilterDialog(
	bigrams: Map<Pair<KC, KC>, Int>,
	initialFilterState: FilterState,
	onDismiss: () -> Unit,
	onConfirm: (FilterState, Map<Pair<KC, KC>, Int>) -> Unit,
) {
	var currentFilterState by remember { mutableStateOf(initialFilterState) }
	var filteredBigrams by remember { mutableStateOf(bigrams) }
	
	Dialog(onDismissRequest = onDismiss) {
		Card(
			modifier = Modifier.fillMaxWidth().padding(16.dp),
			shape = RoundedCornerShape(16.dp),
			backgroundColor = MaterialTheme.colors.surface,
			contentColor = MaterialTheme.colors.onSurface,
			elevation = 0.dp
		) {
			Column(
				modifier = Modifier.padding(16.dp),
				verticalArrangement = Arrangement.spacedBy(16.dp)
			) {
				DialogHeader("Filter bigrams")
				
				Filters(
					filterState = currentFilterState,
					onFilterChanged = { newState ->
						currentFilterState = newState
						filteredBigrams = applyFilters(bigrams, newState)
					}
				)
				
				Row {
					Spacer(modifier = Modifier.weight(1f))
					TextButton(
						onClick = onDismiss,
						colors = ButtonDefaults.textButtonColors(
							contentColor = MaterialTheme.colors.onSurface.copy(alpha = 0.6f)
						)
					) {
						Text("Cancel")
					}
					Spacer(modifier = Modifier.width(8.dp))
					Button(
						onClick = { onConfirm(currentFilterState, filteredBigrams) },
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
private fun Filters(
	filterState: FilterState,
	onFilterChanged: (FilterState) -> Unit,
) {
	Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 30.dp)) {
		// Minimum count filter
		Row(
			modifier = Modifier.fillMaxWidth(),
			horizontalArrangement = Arrangement.SpaceBetween
		) {
			Text(
				modifier = Modifier.align(Alignment.CenterVertically),
				text = "Minimum count:"
			)
			OutlinedTextField(
				value = filterState.minCount.toString(),
				onValueChange = {
					val newState = filterState.copy(minCount = it.toIntOrNull() ?: 0)
					onFilterChanged(newState)
				},
				modifier = Modifier.width(100.dp)
			)
		}
		// Show letters filter
		Row(
			modifier = Modifier.fillMaxWidth(),
			horizontalArrangement = Arrangement.SpaceBetween
		) {
			Text(
				modifier = Modifier.align(Alignment.CenterVertically),
				text = "Letters:"
			)
			Switch(
				checked = filterState.showLetters,
				onCheckedChange = {
					val newState = filterState.copy(showLetters = it)
					onFilterChanged(newState)
				}
			)
		}
		
		// Show navigation filter
		Row(
			modifier = Modifier.fillMaxWidth(),
			horizontalArrangement = Arrangement.SpaceBetween
		) {
			Text(
				modifier = Modifier.align(Alignment.CenterVertically),
				text = "Navigation:"
			)
			Switch(
				checked = filterState.showNavigation,
				onCheckedChange = {
					val newState = filterState.copy(showNavigation = it)
					onFilterChanged(newState)
				}
			)
		}
		
		// Show numbers filter
		Row(
			modifier = Modifier.fillMaxWidth(),
			horizontalArrangement = Arrangement.SpaceBetween
		) {
			Text(
				modifier = Modifier.align(Alignment.CenterVertically),
				text = "Numbers:"
			)
			Switch(
				checked = filterState.showNumbers,
				onCheckedChange = {
					val newState = filterState.copy(showNumbers = it)
					onFilterChanged(newState)
				}
			)
		}
		
		// Show symbols filter
		Row(
			modifier = Modifier.fillMaxWidth(),
			horizontalArrangement = Arrangement.SpaceBetween
		) {
			Text(
				modifier = Modifier.align(Alignment.CenterVertically),
				text = "Symbols:"
			)
			Switch(
				checked = filterState.showSymbols,
				onCheckedChange = {
					val newState = filterState.copy(showSymbols = it)
					onFilterChanged(newState)
				}
			)
		}
		
		// Show modifiers filter
		Row(
			modifier = Modifier.fillMaxWidth(),
			horizontalArrangement = Arrangement.SpaceBetween
		) {
			Text(
				modifier = Modifier.align(Alignment.CenterVertically),
				text = "Modifiers:"
			)
			Switch(
				checked = filterState.showModifiers,
				onCheckedChange = {
					val newState = filterState.copy(showModifiers = it)
					onFilterChanged(newState)
				}
			)
		}
	}
}

private fun applyFilters(
	originalBigrams: Map<Pair<KC, KC>, Int>,
	filterState: FilterState,
): Map<Pair<KC, KC>, Int> {
	return originalBigrams.filter { (bigram, count) ->
		val (first, second) = bigram
		return@filter (count >= filterState.minCount) &&
				(filterState.showModifiers || (!first.isModifier() && !second.isModifier())) &&
				(filterState.showLetters || (!first.isLetter() && !second.isLetter())) &&
				(filterState.showNavigation || (!first.isNavigation() && !second.isNavigation())) &&
				(filterState.showNumbers || (!first.isNumber() && !second.isNumber())) &&
				(filterState.showSymbols || (!first.isSymbol() && !second.isSymbol()))
	}
}

private fun isOnSameHand(first: KC, second: KC): Boolean {
	TODO()
}

private fun isSFB(first: KC, second: KC): Boolean {
	TODO()
}