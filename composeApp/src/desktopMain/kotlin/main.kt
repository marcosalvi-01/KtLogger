import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.window.Tray
import androidx.compose.ui.window.application
import database.Database
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import logger.SystemLogger
import ui.MainWindow
import ui.matteBlueTheme
import java.awt.*
import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes

lateinit var icon: Painter

fun main() {
	// Catch uncaught exceptions and show a dialog with the error
	Thread.setDefaultUncaughtExceptionHandler { _, e ->
		Dialog(Frame(), e.message ?: "Error").apply {
			layout = FlowLayout()
			val label = Label(e.message)
			add(label)
			val button = Button("OK").apply {
				addActionListener { dispose() }
			}
			add(button)
			setSize(300, 300)
			isVisible = true
		}
	}

	// Initialize the db
	Database.connect()
	// Start the logger
	SystemLogger.start()
	// Start the loop to save and load the data
	loopSaveAndLoad()

	application {
		// Remember the state of the main window, heatmap window
		val isMainWindowOpen = remember { mutableStateOf(true) }

		// Get the icon
		icon = painterResource("app_icon.png")

		MaterialTheme(
			colors = matteBlueTheme
		) {
			// The main window
			MainWindow(isMainWindowOpen)

			Tray(
				icon = icon,
				menu = {
					// Pause/resume logging
					Item(if (SystemLogger.isRunning) "Pause logging" else "Resume logging") {
						if (SystemLogger.isRunning)
							SystemLogger.stop()
						else
							SystemLogger.start()
					}
					// Add key presses from file
					Item("Exit") {
						// Close the app
						exitApplication()
					}
				},
				onAction = {
					isMainWindowOpen.value = true
				}
			)
		}
	}
}

val loopDelay: Duration = 5L.minutes

private fun loopSaveAndLoad() {
	// Launch a coroutine
	CoroutineScope(Dispatchers.Default).launch {
		// Loop forever
		while (true) {
			// Delay
			delay(loopDelay.inWholeMilliseconds)
			// Save the data
			SystemLogger.saveData()
			// Reload the windows
			Database.loadData()
		}
	}
}
