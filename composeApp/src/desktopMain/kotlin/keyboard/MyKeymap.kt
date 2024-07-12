package keyboard

private val EMPTY = LayerKey(KC.EMPTY)

enum class MyKeymap(
	val layer: KeyLayer,
) {

	BASE(
		KeyLayer(
			"Base",
			listOf(
				listOf(
					LayerKey(KC.Q),
					LayerKey(KC.W),
					LayerKey(KC.F),
					LayerKey(KC.P),
					LayerKey(KC.B),
					null,
					LayerKey(KC.J),
					LayerKey(KC.H),
					LayerKey(KC.U),
					LayerKey(KC.Y),
					LayerKey(KC.QUESTION_MARK)
				),
				listOf(
					LayerKey(KC.A),
					LayerKey(KC.R),
					LayerKey(KC.S),
					LayerKey(KC.T),
					LayerKey(KC.G),
					null,
					LayerKey(KC.M),
					LayerKey(KC.N),
					LayerKey(KC.E),
					LayerKey(KC.I),
					LayerKey(KC.O)
				),
				listOf(
					LayerKey(KC.Z),
					LayerKey(KC.X),
					LayerKey(KC.C),
					LayerKey(KC.D),
					LayerKey(KC.V),
					null,
					LayerKey(KC.K),
					LayerKey(KC.L),
					LayerKey(KC.PERIOD),
					LayerKey(KC.COMMA),
					LayerKey(KC.MINUS)
				),
				listOf(
					null,
					null,
					LayerKey(KC.DELETE),
					LayerKey(KC.SPACE),
					LayerKey(KC.TAB),
					null,
					LayerKey(KC.ENTER),
					LayerKey(KC.BACKSPACE),
					LayerKey(KC.ESCAPE),
					null,
					null
				)
			)
		)
	),

	SYMBOLS(
		KeyLayer(
			"Symbols",
			listOf(
				listOf(
					EMPTY,
					EMPTY,
					EMPTY,
					EMPTY,
					EMPTY,

					null,

					LayerKey(KC.BACK_QUOTE),
					LayerKey(KC.ASTERISK),
					LayerKey(KC.AT),
					LayerKey(KC.DOLLAR),
					LayerKey(KC.HASH),
				),
				listOf(
					EMPTY,
					EMPTY,
					EMPTY,
					EMPTY,
					EMPTY,

					null,

					LayerKey(KC.LESS),
					LayerKey(KC.EQUAL),
					LayerKey(KC.QUOTE),
					LayerKey(KC.LEFT_CURLY_BRACKET),
					LayerKey(KC.LEFT_SQUARE_BRACKET),
				),
				listOf(
					EMPTY,
					EMPTY,
					EMPTY,
					EMPTY,
					EMPTY,

					null,

					LayerKey(KC.PERCENT),
					LayerKey(KC.SLASH),
					LayerKey(KC.EXCLAMATION_MARK),
					LayerKey(KC.AMPERSAND),
					LayerKey(KC.PIPE),
				),
				listOf(
					null,
					null,
					EMPTY,
					EMPTY,
					EMPTY,

					null,

					LayerKey(KC.PLUS),
					LayerKey(KC.LEFT_PARENTHESIS),
					EMPTY,
					null,
					null,
				)
			)
		)
	),

	NAVIGATION(
		KeyLayer(
			"Navigation",
			listOf(
				listOf(
					EMPTY,
					LayerKey(KC.HOME),
					LayerKey(KC.UP),
					LayerKey(KC.END),
					EMPTY,

					null,

					EMPTY,
					EMPTY,
					EMPTY,
					EMPTY,
					EMPTY,
				),
				listOf(
					LayerKey(KC.PAGE_UP),
					LayerKey(KC.LEFT),
					LayerKey(KC.DOWN),
					LayerKey(KC.RIGHT),
					EMPTY,

					null,

					EMPTY,
					EMPTY,
					EMPTY,
					EMPTY,
					EMPTY,
				),
				listOf(
					LayerKey(KC.PAGE_DOWN),
					EMPTY,
					EMPTY,
					EMPTY,
					EMPTY,

					null,

					EMPTY,
					EMPTY,
					EMPTY,
					EMPTY,
					EMPTY,
				),
				listOf(
					null,
					null,
					EMPTY,
					EMPTY,
					EMPTY,

					null,

					EMPTY,
					EMPTY,
					EMPTY,
					null,
					null,
				)
			)
		)
	),

	NUMBERS(
		KeyLayer(
			"Numbers",
			listOf(
				listOf(
					EMPTY,
					EMPTY,
					EMPTY,
					EMPTY,
					EMPTY,

					null,

					EMPTY,
					LayerKey(KC.SEVEN),
					LayerKey(KC.EIGHT),
					LayerKey(KC.NINE),
					EMPTY,
				),
				listOf(
					EMPTY,
					EMPTY,
					EMPTY,
					EMPTY,
					EMPTY,

					null,

					EMPTY,
					LayerKey(KC.FOUR),
					LayerKey(KC.FIVE),
					LayerKey(KC.SIX),
					LayerKey(KC.CIRCUMFLEX),
				),
				listOf(
					EMPTY,
					EMPTY,
					EMPTY,
					EMPTY,
					EMPTY,

					null,

					EMPTY,
					LayerKey(KC.ONE),
					LayerKey(KC.TWO),
					LayerKey(KC.THREE),
					EMPTY,
				),
				listOf(
					null,
					null,
					LayerKey(KC.EMPTY),
					LayerKey(KC.EMPTY),
					LayerKey(KC.EMPTY),

					null,

					LayerKey(KC.EMPTY),
					LayerKey(KC.ZERO),
					LayerKey(KC.EMPTY),
					null,
					null,
				)
			)
		)
	),

	;

	companion object {
		fun layers() = entries.map { it.layer }
	}
}

val defaultKeymap = Keymap("Default", MyKeymap.layers())