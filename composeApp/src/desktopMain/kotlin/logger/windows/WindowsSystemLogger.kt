package logger.windows

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import logger.ButtonPressEvent
import logger.LoggerData
import logger.MovementEvent
import logger.ScrollWheelEvent
import logger.SystemLogger
import logger.WindowInfo

object WindowsSystemLogger : SystemLogger {
	private val activeWindow = MutableStateFlow(
		LoggerData(
			WindowInfo("KtLogger"),
			Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
		)
	)
	override val dataFlow = activeWindow.asStateFlow()
	
	private lateinit var _isRunning: StateFlow<Boolean>
	override val isRunning: StateFlow<Boolean> by lazy {
		_isRunning
	}
	
	val list: MutableList<WindowInfo> = mutableListOf()
	
	val scope = CoroutineScope(Dispatchers.IO)
	
	override fun start() {
		// The loggers are all started in coroutines
		WindowsKeyPressesLogger.start()
		WindowsMouseLogger.start()
		WindowsWindowLogger.start()
		
		// Start elaborating the data and emitting it
		elaborateData()
		
		// Connect the isRunning of each logger to the isRunning of the SystemLogger
		scope.launch {
			_isRunning = combine(
				WindowsKeyPressesLogger.isRunning,
				WindowsMouseLogger.isRunning,
				WindowsWindowLogger.isRunning,
			) { k, m, w -> k && m && w }.stateIn(CoroutineScope(Dispatchers.Default))
		}
	}
	
	override fun stop() {
		WindowsKeyPressesLogger.stop()
		WindowsMouseLogger.stop()
		WindowsWindowLogger.stop()
	}
	
	fun saveData() {
		// Save the data to the database
		list.forEach { it.save() }
	}
	
	private fun elaborateData() {
		// Increase the key press count of the key in the active window
		scope.launch {
			WindowsKeyPressesLogger.dataFlow.collect { data ->
				getWindowInfo(WindowsWindowLogger.dataFlow.value.data.name)?.addKeyPress(data.data.key)
			}
		}
		// Handle the mouse events
		scope.launch {
			WindowsMouseLogger.dataFlow.collect { (mouseEvent, time) ->
				// Handle the mouseEvent here
				getWindowInfo(WindowsWindowLogger.dataFlow.value.data.name)?.let {
					// Add the mouse event to the window info
					when (mouseEvent) {
						is ButtonPressEvent -> it.addMouseButtonPress(mouseEvent.button)
						is ScrollWheelEvent -> it.addScrollWheelEvent(mouseEvent.direction)
						is MovementEvent -> it.addMouseMovement(mouseEvent.position)
					}
				}
			}
		}
		// Increase the scroll direction count of the direction in the active window
		scope.launch {
			WindowsWindowLogger.dataFlow.collect { (window, time) ->
				// Check if the window is already in the data
				val windowInfo = getWindowInfo(window.name)
				if (windowInfo != null) {
					// Focus the window
					windowFocused(windowInfo)
				} else {
					// Create a new window info
					val newWindowInfo = WindowInfo(window.name)
					// Focus the window
					windowFocused(newWindowInfo)
					// Add the new window info to the data
					list += newWindowInfo
				}
			}
		}
	}
	
	private fun getWindowInfo(windowName: String): WindowInfo? {
		return list.find { it.id == windowName }
	}
	
	private fun windowFocused(newActiveWindow: WindowInfo) {
		// Check if the window is already the active window
		if (newActiveWindow == activeWindow.value.data) return
		
		// Focus the new window
		newActiveWindow.windowFocused()
		// Unfocus the previous window
		activeWindow.value.data.windowUnfocused()
		// Update the active window
		scope.launch {
			activeWindow.emit(
				LoggerData(
					newActiveWindow,
					Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
				)
			)
		}
	}
}