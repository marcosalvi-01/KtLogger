package ui

import androidx.compose.desktop.ui.tooling.preview.Preview
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
@Preview
fun TrigramsWindow(
	trigrams: Map<Triple<KC, KC, KC>, Int>,
) {
	var trigramsToShow: Map<Triple<KC, KC, KC>, Int> by remember {
		mutableStateOf(
			trigrams
		)
	}
	
	val trigramsTotal = trigramsToShow.values.sum()
	
	val showOnlyLetters = remember { mutableStateOf(false) }
	
	Column {
		Button(
			modifier = Modifier.align(Alignment.CenterHorizontally),
			onClick = {
				showOnlyLetters.value = !showOnlyLetters.value
				// filter the bigrams to show only the ones with letters
				trigramsToShow = if (showOnlyLetters.value) {
					trigrams.filter { (bigram, _) ->
						bigram.first.isLetter() && bigram.second.isLetter() && bigram.third.isLetter()
					}
				} else {
					trigrams
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
				KeyColumnItem("Trigram", 0.5f),
				KeyColumnItem("Count", 0.5f),
				KeyColumnItem("Percentage", 0.5f),
			),
			showScrollbar = true,
			data = trigramsToShow.entries.sortedByDescending { it.value }
				.map { (trigram, value) ->
					KeyRowItem(
						listOf(
							KeyColumnItem(
								trigram.first.unicode + " " + trigram.second.unicode + " " + trigram.third.unicode,
								0.5f
							),
							KeyColumnItem(value.toString(), 0.5f),
							KeyColumnItem(
								"${
									String.format(
										"%.2f",
										(value.toDouble() / trigramsTotal * 100)
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