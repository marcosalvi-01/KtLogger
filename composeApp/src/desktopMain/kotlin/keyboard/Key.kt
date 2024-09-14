package keyboard

/**
 * Represents a key in a layout, with its key code and the key code when the shift,
 * altGr, or both keys are pressed.
 *
 * @property keyCode The key code in hex (used by the system).
 * @property kc The key code in the KC enum.
 * @property shift The key code when the shift key is pressed.
 * @property altGr The key code when the altGr key is pressed.
 * @property shiftAltGr The key code when the shift and altGr keys are pressed.
 */
interface Key {
	val keyCode: Int          // The key code in hex (used by the system)
	val kc: KC                 // The key code in the KC enum
	val shift: KC?             // The key code when the shift key is pressed
	val altGr: KC?             // The key code when the altGr key is pressed
	val shiftAltGr: KC?        // The key code when the shift and altGr keys are pressed
	
}