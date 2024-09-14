package logger

import keyboard.KeyPress
import kotlinx.coroutines.flow.SharedFlow

/**
 * Interface for logging key presses, extending the Logger interface for KeyPress events.
 */
interface KeyPressesLogger : Logger<KeyPress> {
	
	override val dataFlow: SharedFlow<LoggerData<KeyPress>>
	
	/**
	 * Checks if the Shift key is currently pressed.
	 *
	 * @return True if the Shift key is down, false otherwise.
	 */
	fun isShiftDown(): Boolean
	
	/**
	 * Checks if the Ctrl key is currently pressed.
	 *
	 * @return True if the Ctrl key is down, false otherwise.
	 */
	fun isCtrlDown(): Boolean
	
	/**
	 * Checks if the Alt key is currently pressed.
	 *
	 * @return True if the Alt key is down, false otherwise.
	 */
	fun isAltDown(): Boolean
	
	/**
	 * Checks if the AltGr key is currently pressed.
	 *
	 * @return True if the AltGr key is down, false otherwise.
	 */
	fun isAltGrDown(): Boolean
	
	/**
	 * Checks if the Gui (Windows/Command) key is currently pressed.
	 *
	 * @return True if the Gui key is down, false otherwise.
	 */
	fun isGuiDown(): Boolean
}