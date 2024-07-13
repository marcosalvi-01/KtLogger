package ui

import androidx.compose.foundation.VerticalScrollbar
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.rememberScrollbarAdapter
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Button
import androidx.compose.material.Card
import androidx.compose.material.Checkbox
import androidx.compose.material.DropdownMenu
import androidx.compose.material.DropdownMenuItem
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.material.TextFieldDefaults
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import database.Database
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import logger.format
import kotlin.time.Duration

class HomeScreen : Screen {
	val timeTotal = Database.getFocusTime()
	private val selected = mutableStateOf(Database.getSelectedWindows())
	
	@Composable
	override fun Content() {
		Home(selected)
	}
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun Home(selected: MutableState<List<String>>) {
	val showEditDialog = remember { mutableStateOf<Pair<String, String>?>(null) }
	
	// Get the data from the database and sort it by duration (<<windowId, windowName>, focusTime>)
	val windows =
		Database.windows.collectAsState(Database.loadWindows()).value.entries.sortedByDescending { it.value }
			.associateBy({ it.key }, { it.value })  // Preserves the order
	
	val navigator = LocalNavigator.currentOrThrow
	
	Column(
		horizontalAlignment = Alignment.CenterHorizontally,
		modifier = Modifier.fillMaxSize()
	) {
		
		val scrollState = rememberScrollState()
		
		Box(Modifier.fillMaxSize()) {
			FlowRow(Modifier.fillMaxSize().verticalScroll(scrollState)) {
				Row(Modifier.fillMaxWidth()) {
					Spacer(Modifier.width(8.dp))
					Row(
						horizontalArrangement = Arrangement.Start,
					) {
						Button(
							onClick = {
								// Go to the window info screen
								navigator.push(
									WindowInfoScreen(
										"Totals",
										Database.getFocusTime(),
										Database.getKeyPresses(),
										Database.getBigrams(),
										Database.getTrigrams(),
										Database.getMouseButtons(),
										Database.getScrollDirections(),
										Database.getMousePositions(),
									)
								)
							}
						) {
							Text("Totals")
						}
						
						Spacer(Modifier.width(8.dp))
						
						Button(
							onClick = {
								// Go to the window info screen
								navigator.push(
									WindowInfoScreen(
										"Selected",
										Database.getFocusTime(selected.value),
										Database.getKeyPresses(selected.value),
										Database.getBigrams(selected.value),
										Database.getTrigrams(selected.value),
										Database.getMouseButtons(selected.value),
										Database.getScrollDirections(selected.value),
										Database.getMousePositions(selected.value),
									)
								)
							}
						) {
							Text("Selected")
						}
						
					}
					
					Spacer(Modifier.weight(1f))
					
					Row(
						horizontalArrangement = Arrangement.End,
					) {
						Button(
							onClick = {
								// Deselect all the windows
								Database.deselectAllWindows()
								selected.value = Database.getSelectedWindows()
							}
						) {
							Text("Deselect all")
						}
					}
					
					Spacer(Modifier.width(16.dp))
				}
				// Display the data
				for ((windowIdName, focusTime) in windows) {
					HomeCard(
						showEditDialog,
						windowIdName.first,
						windowIdName.second,
						focusTime,
						selected
					)
				}
			}
			
			VerticalScrollbar(
				modifier = Modifier.align(Alignment.CenterEnd).fillMaxHeight(),
				adapter = rememberScrollbarAdapter(scrollState)
			)
		}
	}
	
	// Show the dialog to edit the title
	if (showEditDialog.value != null) {
		EditTitleDialog(showEditDialog)
	}
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
private fun HomeCard(
	showEditDialog: MutableState<Pair<String, String>?>,
	windowId: String,
	windowName: String,
	focusTime: Duration,
	selected: MutableState<List<String>>, // The selected cards
) {
	val navigator = LocalNavigator.currentOrThrow
	Card(
		modifier = Modifier
			.fillMaxWidth(0.33f)
			.padding(8.dp)
			.aspectRatio(2f),
		elevation = 4.dp,
		onClick = {
			// Go to the window info screen
			navigator.push(
				WindowInfoScreen(
					windowName,
					focusTime,
					Database.getKeyPresses(windowId),
					Database.getBigrams(windowId),
					Database.getTrigrams(windowId),
					Database.getMouseButtons(windowId),
					Database.getScrollDirections(windowId),
					Database.getMousePositions(windowId),
				)
			)
		}
	) {
		var contextMenuExpanded by remember { mutableStateOf(false) }
		Box(modifier = Modifier.fillMaxSize()) {
			// Text at the top left
			Text(
				text = windowName,
				modifier = Modifier.align(Alignment.TopStart).padding(16.dp)
			)
			
			// Buttons at the top right
			Box(modifier = Modifier.align(Alignment.TopEnd)) {
				// Button at the top right
				IconButton(
					onClick = {
						// Show the context menu
						contextMenuExpanded = true
					},
				) {
					Icon(
						Icons.Filled.MoreVert,
						contentDescription = "More options",
					)
				}
				
				// DropdownMenu remains the same
				DropdownMenu(
					expanded = contextMenuExpanded,
					onDismissRequest = { contextMenuExpanded = false },
				) {
					DropdownMenuItem(onClick = {
						contextMenuExpanded = false
						// Show the dialog to edit the title
						showEditDialog.value = Pair(windowId, windowName)
					}) {
						Text("Edit title")
					}
					DropdownMenuItem(onClick = {
						contextMenuExpanded = false
						// Hide the card
						Database.hideWindow(windowId)
						// Load the data again
						CoroutineScope(Dispatchers.Default).launch {
							Database.loadData()
						}
					}) {
						Text("Hide")
					}
				}
			}
			
			// Time at the bottom left
			Text(
				text = focusTime.format(),
				modifier = Modifier.align(Alignment.BottomStart).padding(16.dp)
			)
			
			// Checkbox at the bottom right
			Checkbox(
				checked = selected.value.contains(windowId),
				onCheckedChange = {
					if (it) {
						Database.selectWindow(windowId)
						selected.value += windowId
					} else {
						Database.deselectWindow(windowId)
						selected.value -= windowId
					}
				},
				modifier = Modifier.align(Alignment.BottomEnd).padding(8.dp)
			)
		}
	}
}


@Composable
private fun EditTitleDialog(
	showEditDialog: MutableState<Pair<String, String>?>,
) {
	// Just to be sure
	if (showEditDialog.value == null) return
	
	var title by remember { mutableStateOf(showEditDialog.value!!.second) }
	
	Dialog(
		onDismissRequest = {
			// Close the dialog
			showEditDialog.value = null
		}
	) {
		Card {
			Column(
				modifier = Modifier.padding(16.dp),
			) {
				// Show a field to edit the title
				Text(
					text = "Edit title",
					modifier = Modifier.padding(8.dp),
					style = MaterialTheme.typography.h6,
					fontWeight = FontWeight.Bold,
				)
				TextField(
					value = title,
					onValueChange = {
						title = it
					},
					modifier = Modifier.fillMaxWidth().padding(8.dp).onKeyEvent {
						if (it.key != Key.Enter) return@onKeyEvent false
						if (it.type == KeyEventType.KeyUp)
							saveChanges(showEditDialog, title.replace("\n", ""))
						true
					},
					colors = TextFieldDefaults.textFieldColors(
						cursorColor = MaterialTheme.colors.onSurface,
					),
				)
				
				Row(
					modifier = Modifier.fillMaxWidth(),
					horizontalArrangement = Arrangement.Center,
				) {
					// Show a button to save the changes
					Button(
						onClick = {
							saveChanges(showEditDialog, title)
						}
					) {
						Text("Save")
					}
					
					Spacer(modifier = Modifier.width(8.dp))
					
					// Show a button to cancel the changes
					Button(
						onClick = {
							// Close the dialog
							showEditDialog.value = null
						}
					) {
						Text("Cancel")
					}
				}
			}
		}
	}
}

private fun saveChanges(showEditDialog: MutableState<Pair<String, String>?>, title: String) {
	// Save the changes
	Database.setWindowName(showEditDialog.value!!.first, title)
	// Load the data again
	CoroutineScope(Dispatchers.Default).launch {
		Database.loadData()
	}
	// Close the dialog
	showEditDialog.value = null
}
