package keyboard

enum class MyKeymap(
	val layer: KeyLayer
) {
	BASE(
		KeyLayer(
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
			listOf(
				listOf(
					null,
					null,
					null,
					null,
					null,

					null,

					LayerKey(KC.BACK_QUOTE),
					LayerKey(KC.ASTERISK),
					LayerKey(KC.AT),
					LayerKey(KC.DOLLAR),
					LayerKey(KC.HASH),
				),
				listOf(
					null,
					null,
					null,
					null,
					null,

					null,

					LayerKey(KC.LESS),
					LayerKey(KC.EQUAL),
					LayerKey(KC.QUOTE),
					LayerKey(KC.LEFT_CURLY_BRACKET),
					LayerKey(KC.LEFT_SQUARE_BRACKET),
				),
				listOf(
					null,
					null,
					null,
					null,
					null,

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
					null,
					null,
					null,

					null,

					LayerKey(KC.PLUS),
					LayerKey(KC.LEFT_PARENTHESIS),
					null,
					null,
					null,
				)
			)
		)
	)

	;

	companion object {
		fun layers() = entries.map { it.layer }
	}
}