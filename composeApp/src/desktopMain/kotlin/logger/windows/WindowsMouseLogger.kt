package logger.windows

import com.sun.jna.Pointer
import com.sun.jna.platform.win32.Kernel32
import com.sun.jna.platform.win32.User32
import com.sun.jna.platform.win32.WinDef
import com.sun.jna.platform.win32.WinUser
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import logger.ButtonPressEvent
import logger.LoggerData
import logger.MouseButton
import logger.MouseEvent
import logger.MouseLogger
import logger.MovementEvent
import logger.Position
import logger.ScrollDirection
import logger.ScrollWheelEvent

object WindowsMouseLogger : MouseLogger {
	private val mouseEvents = MutableSharedFlow<LoggerData<MouseEvent>>()
	override val dataFlow = mouseEvents.asSharedFlow()
	
	private val _isRunning = MutableStateFlow(false)
	override val isRunning = _isRunning.asStateFlow()
	
	private val user32 = User32.INSTANCE
	private var hhk: WinUser.HHOOK? = null
	private val hMod: WinDef.HMODULE = Kernel32.INSTANCE.GetModuleHandle(null)
	private val scope = CoroutineScope(Dispatchers.IO)
	
	// Mouse events
	private const val WM_LBUTTONDOWN = 0x0201
	private const val WM_RBUTTONDOWN = 0x0204
	private const val WM_MBUTTONDOWN = 0x0207
	private const val WM_MOUSEWHEEL = 0x020A
	private const val WM_MOUSEMOVE = 0x0200
	
	override fun start() {
		if (_isRunning.value) return
		_isRunning.value = true
		scope.launch {
			try {
				hhk = user32.SetWindowsHookEx(
					WinUser.WH_MOUSE_LL,
					hook,
					hMod, 0
				)
				val msg = WinUser.MSG()
				while (user32.GetMessage(msg, null, 0, 0) != 0) {
					user32.TranslateMessage(msg)
					user32.DispatchMessage(msg)
				}
			} finally {
				stop()
			}
		}
	}
	
	override fun stop() {
		if (!_isRunning.value) return
		_isRunning.value = false
		if (hhk != null) {
			user32.UnhookWindowsHookEx(hhk)
			hhk = null
		}
	}
	
	private val hook = WinUser.LowLevelMouseProc { nCode, wParam, info ->
		if (nCode >= 0) {
			when (wParam.toInt()) {
				WM_LBUTTONDOWN -> {
					emitMouseEvent(ButtonPressEvent(MouseButton.LEFT))
				}
				
				WM_RBUTTONDOWN -> {
					emitMouseEvent(ButtonPressEvent(MouseButton.RIGHT))
				}
				
				WM_MBUTTONDOWN -> {
					emitMouseEvent(ButtonPressEvent(MouseButton.MIDDLE))
				}
				
				WM_MOUSEWHEEL -> {
					if (info.mouseData > 0)
						emitMouseEvent(ScrollWheelEvent(ScrollDirection.UP))
					else
						emitMouseEvent(ScrollWheelEvent(ScrollDirection.DOWN))
				}
				
				WM_MOUSEMOVE -> {
					val x = info.pt.x
					val y = info.pt.y
					if (x >= 0 && y >= 0)
						emitMouseEvent(MovementEvent(Position(x, y)))
				}
			}
		}
		user32.CallNextHookEx(
			null,
			nCode,
			wParam,
			WinDef.LPARAM(Pointer.nativeValue(info.pointer))
		)
	}
	
	private fun emitMouseEvent(event: MouseEvent) {
		scope.launch {
			mouseEvents.emit(
				LoggerData(
					event,
					Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
				)
			)
		}
	}
}

