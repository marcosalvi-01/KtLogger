package keyboard

/**
 * Represents a key press, not just a key code
 *
 * It could be a single key press or a key press with modifiers
 */
data class KeyPress(
	val key: KC,
	val isShiftDown: Boolean,
	val isAltDown: Boolean,
	val isCtrlDown: Boolean,
	val isGuiDown: Boolean,
)
