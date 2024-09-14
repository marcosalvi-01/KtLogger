package logger.windows

import com.sun.jna.platform.win32.Kernel32
import com.sun.jna.platform.win32.Psapi
import com.sun.jna.platform.win32.User32
import com.sun.jna.platform.win32.WinNT
import com.sun.jna.ptr.IntByReference
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import logger.LoggerData
import logger.WindowLogger

data class Window(val name: String)

object WindowsWindowLogger : WindowLogger {
	private val activeWindow = MutableStateFlow<LoggerData<Window>>(
		LoggerData(
			Window("KtLogger"),
			Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
		)
	)
	override val dataFlow = activeWindow.asStateFlow()
	
	private val _isRunning = MutableStateFlow(false)
	override val isRunning = _isRunning.asStateFlow()
	
	override fun start() {
		if (_isRunning.value) return
		_isRunning.value = true
		
		CoroutineScope(Dispatchers.IO).launch {
			try {
				while (_isRunning.value) {
					val windowName = getForegroundWindowName()
					if (windowName.isNotEmpty() && windowName != activeWindow.value.data.name) {
						activeWindow.emit(
							LoggerData(
								Window(windowName),
								Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
							)
						)
					}
				}
			} finally {
				stop()
			}
		}
	}
	
	override fun stop() {
		if (!_isRunning.value) return
		_isRunning.value = false
	}
	
	private fun getForegroundWindowName(): String {
		val processId = IntByReference()
		User32.INSTANCE.GetWindowThreadProcessId(User32.INSTANCE.GetForegroundWindow(), processId)
		val process =
			Kernel32.INSTANCE.OpenProcess(WinNT.PROCESS_QUERY_INFORMATION, false, processId.value)
		val filePath = CharArray(1024)
		Psapi.INSTANCE.GetModuleFileNameExW(process, null, filePath, filePath.size)
		Kernel32.INSTANCE.CloseHandle(process)
		val processFilePath =
			filePath.joinToString("").replace("\u0000", "")
		return processFilePath.substringAfterLast('\\')
	}
}
