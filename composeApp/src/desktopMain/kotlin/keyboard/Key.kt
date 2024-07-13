package keyboard

enum class Key(
	val keyCode: Int? = null,   // The key code in hex (used by the system)
	val kc: KC,         // The key code in the KC enum
	val shift: KC? = null,      // The key code when the shift key is pressed
	val altGr: KC? = null,      // The key code when the altGr key is pressed
	val shiftAltGr: KC? = null  // The key code when the shift and altGr keys are pressed
) {
	// -------- LETTERS --------
	A(0x41, KC.A),
	B(0x42, KC.B),
	C(0x43, KC.C),
	D(0x44, KC.D),
	E(0x45, KC.E),
	F(0x46, KC.F),
	G(0x47, KC.G),
	H(0x48, KC.H),
	I(0x49, KC.I),
	J(0x4A, KC.J),
	K(0x4B, KC.K),
	L(0x4C, KC.L),
	M(0x4D, KC.M),
	N(0x4E, KC.N),
	O(0x4F, KC.O),
	P(0x50, KC.P),
	Q(0x51, KC.Q),
	R(0x52, KC.R),
	S(0x53, KC.S),
	T(0x54, KC.T),
	U(0x55, KC.U),
	V(0x56, KC.V),
	W(0x57, KC.W),
	X(0x58, KC.X),
	Y(0x59, KC.Y),
	Z(0x5A, KC.Z),

	// -------- UTILITIES --------
	SPACE(0x20, KC.SPACE),
	BACKSPACE(0x08, KC.BACKSPACE),
	TAB(0x09, KC.TAB),
	ENTER(0x0D, KC.ENTER),
	ESCAPE(0x1B, KC.ESCAPE),
	DELETE(0x2E, KC.DELETE),
	INSERT(0x2D, KC.INSERT),

	// -------- NAVIGATION --------
	LEFT(0x25, KC.LEFT),
	UP(0x26, KC.UP),
	RIGHT(0x27, KC.RIGHT),
	DOWN(0x28, KC.DOWN),
	HOME(0x24, KC.HOME),
	END(0x23, KC.END),
	PAGE_UP(0x21, KC.PAGE_UP),
	PAGE_DOWN(0x22, KC.PAGE_DOWN),

	// -------- MEDIA --------
	PAUSE_PLAY(0xB3, KC.PAUSE_PLAY),
	NEXT_TRACK(0xB0, KC.NEXT_TRACK),
	PREVIOUS_TRACK(0xB1, KC.PREVIOUS_TRACK),
	MUTE(0xAD, KC.MUTE),
	VOLUME_UP(0xAF, KC.VOLUME_UP),
	VOLUME_DOWN(0xAE, KC.VOLUME_DOWN),

	// -------- MODIFIERS --------
	SHIFT_LEFT(0xA0, KC.SHIFT_LEFT),
	SHIFT_RIGHT(0xA1, KC.SHIFT_RIGHT),
	CTRL_LEFT(0xA2, KC.CTRL_LEFT),
	CTRL_RIGHT(0xA3, KC.CTRL_RIGHT),
	ALT_LEFT(0xA4, KC.ALT_LEFT),
	ALT_RIGHT(0xA5, KC.ALT_RIGHT),
	WIN_LEFT(0x5B, KC.GUI_LEFT),
	WIN_RIGHT(0x5C, KC.GUI_RIGHT),

	// -------- NUMBERS --------
	ZERO(0x30, KC.ZERO, KC.EQUAL),
	ONE(0x31, KC.ONE, KC.EXCLAMATION_MARK),
	TWO(0x32, KC.TWO, KC.QUOTE),
	THREE(0x33, KC.THREE, KC.POUND),
	FOUR(0x34, KC.FOUR, KC.DOLLAR),
	FIVE(0x35, KC.FIVE, KC.PERCENT),
	SIX(0x36, KC.SIX, KC.AMPERSAND),
	SEVEN(0x37, KC.SEVEN, KC.SLASH),
	EIGHT(0x38, KC.EIGHT, KC.LEFT_PARENTHESIS),
	NINE(0x39, KC.NINE, KC.RIGHT_PARENTHESIS),

	// -------- NUMPAD --------
	NUMPAD_0(0x60, KC.NUMPAD_0),
	NUMPAD_1(0x61, KC.NUMPAD_1),
	NUMPAD_2(0x62, KC.NUMPAD_2),
	NUMPAD_3(0x63, KC.NUMPAD_3),
	NUMPAD_4(0x64, KC.NUMPAD_4),
	NUMPAD_5(0x65, KC.NUMPAD_5),
	NUMPAD_6(0x66, KC.NUMPAD_6),
	NUMPAD_7(0x67, KC.NUMPAD_7),
	NUMPAD_8(0x68, KC.NUMPAD_8),
	NUMPAD_9(0x69, KC.NUMPAD_9),

	// -------- SYMBOLS --------
	COMMA(0xBC, KC.COMMA, KC.SEMICOLON),
	PERIOD(0xBE, KC.PERIOD, KC.COLON),
	BACK_SLASH(0xDC, KC.BACK_SLASH, KC.PIPE),
	MINUS(0xBD, KC.MINUS, KC.UNDERSCORE),
	APOSTROPHE(0xDB, KC.APOSTROPHE, KC.QUESTION_MARK),
	PLUS(0xBB, KC.PLUS, KC.ASTERISK, KC.RIGHT_SQUARE_BRACKET, KC.RIGHT_CURLY_BRACKET),
	LESS(0xE2, KC.LESS, KC.GREATER),

	// -------- GRAVED LETTERS --------
	A_GRAVE(0xDE, KC.A_GRAVE, KC.DEGREE, KC.HASH),
	E_GRAVE(0xBA, KC.E_GRAVE, KC.E_ACUTE, KC.LEFT_SQUARE_BRACKET, KC.LEFT_CURLY_BRACKET),
	I_GRAVE(0xDD, KC.I_GRAVE, KC.CIRCUMFLEX),
	O_GRAVE(0xC0, KC.O_GRAVE, KC.CEDILLA, KC.AT),
	U_GRAVE(0xBF, KC.U_GRAVE, KC.SECTION),

	// -------- FUNCTION --------
	F1(0x70, KC.F1),
	F2(0x71, KC.F2),
	F3(0x72, KC.F3),
	F4(0x73, KC.F4),
	F5(0x74, KC.F5),
	F6(0x75, KC.F6),
	F7(0x76, KC.F7),
	F8(0x77, KC.F8),
	F9(0x78, KC.F9),
	F10(0x79, KC.F10),
	F11(0x7A, KC.F11),
	F12(0x7B, KC.F12),

	// -------- LOCK MODIFIERS --------
	CAPS_LOCK(0x14, KC.CAPS_LOCK),
	NUM_LOCK(0x90, KC.NUM_LOCK),
	SCROLL_LOCK(0x91, KC.SCROLL_LOCK),


	// -------- UNKNOWN --------
	UNKNOWN(kc = KC.UNKNOWN),
	;

	companion object {

		// Get the key from the key code, if it does not exist, return UNKNOWN
		fun getKey(keyCode: Int): Key {
			return entries.find { it.keyCode == keyCode } ?: UNKNOWN
		}

		// Return the KC of the pressed key based on the modifiers
		fun getKC(keyCode: Int, shift: Boolean = false, altGr: Boolean = false): KC {
			val key = getKey(keyCode)
			return when {
				// Return the modified KC if it exists, otherwise return the KC
				shift && altGr -> key.shiftAltGr ?: key.kc
				shift -> key.shift ?: key.kc
				altGr -> key.altGr ?: key.kc
				else -> key.kc
			}
		}
	}
}