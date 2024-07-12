package keyboard


enum class KC(
	val symbol: String,
) {
	// -------- LETTERS --------
	A("A"),
	B("B"),
	C("C"),
	D("D"),
	E("E"),
	F("F"),
	G("G"),
	H("H"),
	I("I"),
	J("J"),
	K("K"),
	L("L"),
	M("M"),
	N("N"),
	O("O"),
	P("P"),
	Q("Q"),
	R("R"),
	S("S"),
	T("T"),
	U("U"),
	V("V"),
	W("W"),
	X("X"),
	Y("Y"),
	Z("Z"),

	// -------- SYMBOLS --------
	COMMA(","),
	SEMICOLON(";"),
	PERIOD("."),
	COLON(":"),
	MINUS("-"),
	UNDERSCORE("_"),
	APOSTROPHE("'"),
	QUESTION_MARK("?"),
	BACK_SLASH("\\"),
	SLASH("/"),
	PIPE("|"),
	EXCLAMATION_MARK("!"),
	AT("@"),
	HASH("#"),
	DOLLAR("$"),
	PERCENT("%"),
	CIRCUMFLEX("^"),
	AMPERSAND("&"),
	ASTERISK("*"),
	LEFT_PARENTHESIS("("),
	RIGHT_PARENTHESIS(")"),
	LEFT_SQUARE_BRACKET("["),
	RIGHT_SQUARE_BRACKET("]"),
	LEFT_CURLY_BRACKET("{"),
	RIGHT_CURLY_BRACKET("}"),
	LESS("<"),
	GREATER(">"),
	PLUS("+"),
	EQUAL("="),
	BACK_QUOTE("`"),
	TILDE("~"),
	QUOTE("\""),
	POUND("£"),
	EURO("€"),
	DEGREE("°"),
	SECTION("§"),
	CEDILLA("ç"),


	// -------- UTILITIES --------
	SPACE("Space"),
	ENTER("Enter"),
	TAB("Tab"),
	BACKSPACE("Backspace"),
	ESCAPE("Escape"),
	DELETE("Delete"),
	INSERT("Insert"),

	// -------- NAVIGATION --------
	HOME("Home"),
	END("End"),
	PAGE_UP("Page Up"),
	PAGE_DOWN("Page Down"),
	UP("Up"),
	DOWN("Down"),
	LEFT("Left"),
	RIGHT("Right"),

	// -------- MEDIA KEYS --------
	PAUSE_PLAY("Pause"),
	NEXT_TRACK("Next Track"),
	PREVIOUS_TRACK("Previous Track"),
	MUTE("Mute"),
	VOLUME_UP("Volume Up"),
	VOLUME_DOWN("Volume Down"),

	// -------- LOCK MODIFIERS --------
	CAPS_LOCK("Caps Lock"),
	NUM_LOCK("Num Lock"),
	SCROLL_LOCK("Scroll Lock"),
	PRINT_SCREEN("Print Screen"),

	// -------- FUNCTION KEYS --------
	F1("F1"),
	F2("F2"),
	F3("F3"),
	F4("F4"),
	F5("F5"),
	F6("F6"),
	F7("F7"),
	F8("F8"),
	F9("F9"),
	F10("F10"),
	F11("F11"),
	F12("F12"),

	// -------- NUMBERS --------
	ONE("1"),
	TWO("2"),
	THREE("3"),
	FOUR("4"),
	FIVE("5"),
	SIX("6"),
	SEVEN("7"),
	EIGHT("8"),
	NINE("9"),
	ZERO("0"),

	// -------- NUMPAD --------
	NUMPAD_0("Numpad 0"),
	NUMPAD_1("Numpad 1"),
	NUMPAD_2("Numpad 2"),
	NUMPAD_3("Numpad 3"),
	NUMPAD_4("Numpad 4"),
	NUMPAD_5("Numpad 5"),
	NUMPAD_6("Numpad 6"),
	NUMPAD_7("Numpad 7"),
	NUMPAD_8("Numpad 8"),
	NUMPAD_9("Numpad 9"),

	// -------- MODIFIERS --------
	SHIFT_LEFT("Shift"),
	SHIFT_RIGHT("Shift"),
	CTRL_LEFT("Control"),
	CTRL_RIGHT("Control"),
	ALT_LEFT("Alt"),
	ALT_RIGHT("Alt"),
	WIN_LEFT("Windows"),
	WIN_RIGHT("Windows"),

	// -------- GRAVED LETTERS --------
	A_GRAVE("à"),
	E_GRAVE("è"),
	E_ACUTE("é"),
	I_GRAVE("ì"),
	O_GRAVE("ò"),
	U_GRAVE("ù"),

	// -------- UNKNOWN --------
	UNKNOWN("Unknown"),

	// -------- EMPTY --------
	EMPTY("")

	;

	fun isModifier(): Boolean {
		return this in listOf(
			SHIFT_LEFT,
			SHIFT_RIGHT,
			CTRL_LEFT,
			CTRL_RIGHT,
			ALT_LEFT,
			ALT_RIGHT,
			WIN_LEFT,
			WIN_RIGHT
		)
	}

	fun isLetter(): Boolean {
		return this in listOf(
			A, B, C, D, E, F, G, H, I, J, K, L, M, N, O, P, Q, R, S, T, U, V, W, X, Y, Z,
			A_GRAVE, E_GRAVE, E_ACUTE, I_GRAVE, O_GRAVE, U_GRAVE
		)
	}

	companion object {
		fun getKC(symbol: String): KC {
			return entries.find { it.symbol == symbol } ?: UNKNOWN
		}
	}
}