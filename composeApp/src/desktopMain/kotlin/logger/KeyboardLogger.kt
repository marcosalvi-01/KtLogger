package logger

import com.sun.jna.Pointer
import com.sun.jna.platform.win32.Kernel32
import com.sun.jna.platform.win32.User32
import com.sun.jna.platform.win32.WinDef
import com.sun.jna.platform.win32.WinUser
import keyboard.KC
import keyboard.Key
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch

object KeyboardLogger {
	// The flow of the key presses
	private val _keyPresses = MutableSharedFlow<KC>()
	val keyPresses = _keyPresses.asSharedFlow()

	// Stuff used for the hook
	private val user32 = User32.INSTANCE
	private var hhk: WinUser.HHOOK? = null
	private val hMod: WinDef.HMODULE = Kernel32.INSTANCE.GetModuleHandle(null)
	var isRunning = false
		private set

	private val scope = CoroutineScope(Dispatchers.IO)

	private val hook = WinUser.LowLevelKeyboardProc { nCode, wParam, info ->
		if (nCode >= 0) {
			when (wParam.toInt()) {
				WinUser.WM_KEYUP, WinUser.WM_SYSKEYUP -> {
					// Handle the key press
					scope.launch {
						val kc = Key.getKC(info.vkCode, isShiftDown(), isAltGrDown())
						_keyPresses.emit(kc)
					}
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

	fun start() {
		if (isRunning) return
		isRunning = true
		scope.launch {
			hhk = user32.SetWindowsHookEx(WinUser.WH_KEYBOARD_LL, hook, hMod, 0)
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

	fun isShiftDown(): Boolean {
		return User32.INSTANCE.GetAsyncKeyState(WinUser.VK_SHIFT).toInt() and 0x8000 != 0
	}

	fun isAltGrDown(): Boolean {
		return User32.INSTANCE.GetAsyncKeyState(WinUser.VK_MENU).toInt() and 0x8000 != 0
	}
}