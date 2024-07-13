package logger

import com.sun.jna.platform.win32.Kernel32
import com.sun.jna.platform.win32.Psapi
import com.sun.jna.platform.win32.User32
import com.sun.jna.platform.win32.WinNT
import com.sun.jna.ptr.IntByReference
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch

data class Window(val name: String)

object WindowLogger {
	// The flow of the active windows
	private val _activeWindows = MutableSharedFlow<Window>()
	val activeWindows = _activeWindows.asSharedFlow()
	
	var activeWindow = ""
	
	// Stuff used for the hook
	private val _isRunning = MutableStateFlow(false)
	val isRunning = _isRunning as StateFlow<Boolean>
	
	fun start() {
		if (_isRunning.value) return
		_isRunning.value = true
		CoroutineScope(Dispatchers.IO).launch {
			try {
				while (_isRunning.value) {
					val windowName = getForegroundWindowName()
					if (windowName.isNotEmpty() && windowName != activeWindow) {
						activeWindow = windowName
						_activeWindows.emit(Window(windowName))
					}
					delay(500)
				}
			} catch (e: Exception) {
				stop()
				throw e
			}
		}
	}
	
	fun stop() {
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