package logger

import kotlinx.coroutines.flow.StateFlow

interface SystemLogger : Logger<WindowInfo> {
	override val dataFlow: StateFlow<LoggerData<WindowInfo>>
}