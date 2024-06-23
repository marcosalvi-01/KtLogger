package logger

import com.sun.jna.Pointer
import com.sun.jna.platform.win32.Kernel32
import com.sun.jna.platform.win32.User32
import com.sun.jna.platform.win32.WinDef
import com.sun.jna.platform.win32.WinUser
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch

sealed class MouseEvent
data class ButtonPressEvent(val button: MouseButton) : MouseEvent()
data class ScrollWheelEvent(val direction: ScrollDirection) : MouseEvent()
data class MovementEvent(val position: Position) : MouseEvent()

enum class MouseButton {
	LEFT,
	RIGHT,
	MIDDLE;

	companion object {
		fun getMouseButton(s: String): MouseButton {
			return when (s) {
				"LEFT" -> LEFT
				"RIGHT" -> RIGHT
				"MIDDLE" -> MIDDLE
				else -> throw IllegalArgumentException("Invalid mouse button")
			}
		}
	}
}

enum class ScrollDirection {
	UP,
	DOWN;

	companion object {
		fun getScrollDirection(s: String): ScrollDirection {
			return when (s) {
				"UP" -> UP
				"DOWN" -> DOWN
				else -> throw IllegalArgumentException("Invalid scroll direction")
			}
		}
	}
}

object MouseLogger {
	// The flow of the mouse events
	private val _mouseEvents = MutableSharedFlow<MouseEvent>()
	val mouseEvents = _mouseEvents.asSharedFlow()

	// Stuff used for the hook
	private val user32 = User32.INSTANCE
	private var hhk: WinUser.HHOOK? = null
	private val hMod: WinDef.HMODULE = Kernel32.INSTANCE.GetModuleHandle(null)
	private val scope = CoroutineScope(Dispatchers.IO)
	var isRunning = false
		private set

	// Mouse events
	private const val WM_LBUTTONDOWN = 0x0201
	private const val WM_RBUTTONDOWN = 0x0204
	private const val WM_MBUTTONDOWN = 0x0207
	private const val WM_MOUSEWHEEL = 0x020A
	private const val WM_MOUSEMOVE = 0x0200

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
		user32.CallNextHookEx(null, nCode, wParam, WinDef.LPARAM(Pointer.nativeValue(info.pointer)))
	}

	fun start() {
		if (isRunning) return
		isRunning = true
		scope.launch {
			hhk = user32.SetWindowsHookEx(WinUser.WH_MOUSE_LL, hook, hMod, 0)
			val msg = WinUser.MSG()
			while (user32.GetMessage(msg, null, 0, 0) != 0) {
				user32.TranslateMessage(msg)
				user32.DispatchMessage(msg)
			}
		}
	}

	fun stop() {
		if (!isRunning) return
		isRunning = false
		if (hhk != null) {
			user32.UnhookWindowsHookEx(hhk)
			hhk = null
		}
	}

	private fun emitMouseEvent(event: MouseEvent) {
		scope.launch {
			_mouseEvents.emit(event)
		}
	}
}