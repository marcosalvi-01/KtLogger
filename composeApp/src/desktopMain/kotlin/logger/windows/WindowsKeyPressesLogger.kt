package logger.windows

import com.sun.jna.Pointer
import com.sun.jna.platform.win32.Kernel32
import com.sun.jna.platform.win32.User32
import com.sun.jna.platform.win32.WinDef
import com.sun.jna.platform.win32.WinUser
import keyboard.KC
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import logger.LoggerData
import keyboard.layout.ISOItalian
import keyboard.KeyPress
import logger.KeyPressesLogger

const val TAPPING_TERM = 200

/**
 * Object that logs key presses on Windows using a low-level keyboard hook.
 * Implements the KeyPressesLogger interface.
 */
object WindowsKeyPressesLogger : KeyPressesLogger {
	// SharedFlow to emit key press events
	private val keyPresses = MutableSharedFlow<LoggerData<KeyPress>>()
	override val dataFlow = keyPresses.asSharedFlow()
	
	// StateFlow to track whether the logger is running
	private val _isRunning = MutableStateFlow(false)
	override val isRunning = _isRunning.asStateFlow()
	
	// Windows API instances
	private val user32 = User32.INSTANCE
	private var hhk: WinUser.HHOOK? = null
	private val hMod: WinDef.HMODULE = Kernel32.INSTANCE.GetModuleHandle(null)
	
	// Coroutine scope for asynchronous operations
	private val scope = CoroutineScope(Dispatchers.IO)
	
	// Set to keep track of currently pressed keys
	private val pressedKeysBuffer = mutableListOf<Pair<KC, Instant>>()
	
	/**
	 * Low-level keyboard hook procedure
	 */
	private val hook = WinUser.LowLevelKeyboardProc { nCode, wParam, info ->
		if (nCode >= 0) {
			val kc = ISOItalian.getKC(info.vkCode, isShiftDown(), isCtrlDown() && isAltDown())
			when (wParam.toInt()) {
				// Add the key code to the set of pressed keys
				WinUser.WM_KEYDOWN, WinUser.WM_SYSKEYDOWN -> pressedKeysBuffer.addLast(kc to Clock.System.now())
				WinUser.WM_KEYUP, WinUser.WM_SYSKEYUP -> handleKeyUp(kc)
			}
		}
		user32.CallNextHookEx(
			null,
			nCode,
			wParam,
			WinDef.LPARAM(Pointer.nativeValue(info.pointer))
		)
	}
	
	private fun handleKeyUp(releasedKC: KC) {
		// Remove the key code from the set of pressed keys and emit the key press event
		val now = Clock.System.now()
		val pressedKeysToRemove = mutableSetOf<Int>()
		println(pressedKeysBuffer)
		pressedKeysBuffer.forEachIndexed { i, (kc, instant) ->
			if (kc == releasedKC) {
				if ((now - instant).inWholeMilliseconds <= TAPPING_TERM) {
					scope.launch {
						keyPresses.emit(
							LoggerData(
								KeyPress(
									kc,
									isShiftDown(),
									isAltDown(),
									isCtrlDown(),
									isGuiDown()
								),
								now.toLocalDateTime(TimeZone.currentSystemDefault())
							)
						)
					}
					if (i > 0)
						for (j in 0 until i)
							pressedKeysToRemove.add(j)
					pressedKeysToRemove.add(i)
				} else
					pressedKeysToRemove.add(i)
				return@forEachIndexed
			}
		}
		pressedKeysToRemove.sortedDescending().forEach { pressedKeysBuffer.removeAt(it) }
	}
	
	/**
	 * Starts the key press logger
	 */
	override fun start() {
		if (_isRunning.value) return
		_isRunning.value = true
		scope.launch {
			try {
				hhk = user32.SetWindowsHookEx(WinUser.WH_KEYBOARD_LL, hook, hMod, 0)
				val msg = WinUser.MSG()
				while (user32.GetMessage(msg, null, 0, 0) != 0) {
					user32.TranslateMessage(msg)
					user32.DispatchMessage(msg)
				}
			} catch (e: Exception) {
				stop()
				throw e
			}
		}
	}
	
	/**
	 * Stops the key press logger
	 */
	override fun stop() {
		if (!_isRunning.value) return
		_isRunning.value = false
		hhk?.let { user32.UnhookWindowsHookEx(it) }
		hhk = null
	}
	
	// Methods to check the state of modifier keys
	override fun isShiftDown() = isKeyDown(WinUser.VK_SHIFT)
	override fun isCtrlDown() = isKeyDown(WinUser.VK_CONTROL)
	override fun isAltDown() = isKeyDown(WinUser.VK_MENU)
	override fun isAltGrDown() = isAltDown() && isCtrlDown()
	override fun isGuiDown() = isKeyDown(WinUser.MOD_WIN)
	
	/**
	 * Checks if a key is currently pressed
	 * @param keyCode The virtual key code to check
	 * @return true if the key is pressed, false otherwise
	 */
	private fun isKeyDown(keyCode: Int) =
		user32.GetAsyncKeyState(keyCode).toInt() and 0x8000 != 0
}