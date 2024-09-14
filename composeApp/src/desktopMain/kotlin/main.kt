import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
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
import logger.windows.WindowsSystemLogger
import ui.MainWindow
import ui.matteBlueTheme
import java.awt.Button
import java.awt.Dialog
import java.awt.FlowLayout
import java.awt.Frame
import java.awt.Label
import java.io.File
import java.io.IOException
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
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
		e.printStackTrace()
		
		// Save the error log to a file
		logExceptionToFile(e)
	}
	
	// Initialize the db
	Database.connect()
	// Start the logger
	WindowsSystemLogger.start()
	// Start the loop to save and load the data
	loopSaveAndLoad()
	
	application {
		// Remember the state of the main window, heatmap window
		val isMainWindowOpen = remember { mutableStateOf(true) }
		
		// Get the icon
		icon = painterResource("app_icon.png")
		
		MaterialTheme(
			colors = matteBlueTheme,
		) {
			// The main window
			MainWindow(isMainWindowOpen)
			
			Tray(
				icon = icon,
				menu = {
					val isRunning by WindowsSystemLogger.isRunning.collectAsState()
					Item(
						if (isRunning) "Stop Logger" else "Start Logger"
					) {
						// Start or stop the logger
						if (isRunning)
							WindowsSystemLogger.stop()
						else
							WindowsSystemLogger.start()
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
			WindowsSystemLogger.saveData()
			// Reload the windows
			Database.loadData()
		}
	}
}

fun logExceptionToFile(e: Throwable) {
	val logFile = File("error.log")
	try {
		// Ensure the log file exists
		if (!logFile.exists()) {
			logFile.createNewFile()
		}
		// Format the current timestamp and exception details
		val currentDateTime =
			LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
		val logMessage = "$currentDateTime - ${e.message}\n${e.stackTraceToString()}\n\n"
		
		// Append the formatted exception information to the log file
		logFile.appendText(logMessage)
	} catch (ioException: IOException) {
		ioException.printStackTrace()
	}
}
