package ui

import androidx.compose.foundation.layout.Column
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import keyboard.KC

@Composable
fun BigramsWindow(
	bigrams: Map<Pair<KC, KC>, Int>,
) {
	var bigramsToShow: Map<Pair<KC, KC>, Int> by remember { mutableStateOf(bigrams) }
	
	val bigramsTotal = bigramsToShow.values.sum()
	
	val showOnlyLetters = remember { mutableStateOf(false) }
	
	Column {
		Button(
			modifier = Modifier.align(Alignment.CenterHorizontally),
			onClick = {
				showOnlyLetters.value = !showOnlyLetters.value
				// filter the bigrams to show only the ones with letters
				bigramsToShow = if (showOnlyLetters.value) {
					bigrams.filter { (bigram, _) ->
						bigram.first.isLetter() && bigram.second.isLetter()
					}
				} else {
					bigrams
				}
			},
			colors = if (showOnlyLetters.value) {
				ButtonDefaults.buttonColors(backgroundColor = MaterialTheme.colors.secondary)
			} else {
				ButtonDefaults.buttonColors()
			}
		) {
			Text("Show only letters")
		}
		
		Table(
			columns = listOf(
				KeyColumnItem("Bigram", 0.5f),
				KeyColumnItem("Count", 0.5f),
				KeyColumnItem("Percentage", 0.5f),
			),
			showScrollbar = true,
			data = bigramsToShow.entries.sortedByDescending { it.value }
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
										(value.toDouble() / bigramsTotal * 100)
									)
								}%",
								0.5f
							)
						)
					)
				}) {
			
			for (column in it.getColumns()) {
				TableCell(text = column.getName(), weight = column.getWeight())
			}
		}
	}
}