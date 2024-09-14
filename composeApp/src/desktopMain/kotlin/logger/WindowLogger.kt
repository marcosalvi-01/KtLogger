package logger

import kotlinx.coroutines.flow.StateFlow
import logger.windows.Window

interface WindowLogger : Logger<Window> {
	override val dataFlow: StateFlow<LoggerData<Window>>
}