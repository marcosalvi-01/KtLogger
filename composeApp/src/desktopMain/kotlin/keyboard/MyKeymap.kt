package keyboard

val defaultKeymap = SplitKeymap(
	"Default",
	3,
	5,
	3,
).apply {
	addLayer("Base").apply {
		setKey(0, 0, LayerKey(KC.Q))
		setKey(0, 1, LayerKey(KC.W))
		setKey(0, 2, LayerKey(KC.F))
		setKey(0, 3, LayerKey(KC.P))
		setKey(0, 4, LayerKey(KC.B))
		setKey(0, 6, LayerKey(KC.J))
		setKey(0, 7, LayerKey(KC.H))
		setKey(0, 8, LayerKey(KC.U))
		setKey(0, 9, LayerKey(KC.Y))
		setKey(0, 10, LayerKey(KC.QUESTION_MARK, KC.APOSTROPHE))
		setKey(1, 0, LayerKey(KC.A))
		setKey(1, 1, LayerKey(KC.R))
		setKey(1, 2, LayerKey(KC.S))
		setKey(1, 3, LayerKey(KC.T))
		setKey(1, 4, LayerKey(KC.G))
		setKey(1, 6, LayerKey(KC.M))
		setKey(1, 7, LayerKey(KC.N))
		setKey(1, 8, LayerKey(KC.E))
		setKey(1, 9, LayerKey(KC.I))
		setKey(1, 10, LayerKey(KC.O))
		setKey(2, 0, LayerKey(KC.Z))
		setKey(2, 1, LayerKey(KC.X))
		setKey(2, 2, LayerKey(KC.C))
		setKey(2, 3, LayerKey(KC.D))
		setKey(2, 4, LayerKey(KC.V))
		setKey(2, 6, LayerKey(KC.K))
		setKey(2, 7, LayerKey(KC.L))
		setKey(2, 8, LayerKey(KC.PERIOD, KC.COLON))
		setKey(2, 9, LayerKey(KC.COMMA, KC.SEMICOLON))
		setKey(2, 10, LayerKey(KC.MINUS, KC.UNDERSCORE))
		
		setKey(3, 2, LayerKey(KC.DELETE))
		setKey(3, 3, LayerKey(KC.SPACE))
		setKey(3, 4, LayerKey(KC.TAB))
		setKey(3, 6, LayerKey(KC.ENTER))
		setKey(3, 7, LayerKey(KC.BACKSPACE))
		setKey(3, 8, LayerKey(KC.ESCAPE))
	}
	addLayer("Symbols").apply {
		setKey(0, 6, LayerKey(KC.TILDE))
		setKey(0, 7, LayerKey(KC.ASTERISK))
		setKey(0, 8, LayerKey(KC.AT))
		setKey(0, 9, LayerKey(KC.DOLLAR))
		setKey(0, 10, LayerKey(KC.HASH))
		
		setKey(1, 6, LayerKey(KC.LESS, KC.GREATER))
		setKey(1, 7, LayerKey(KC.EQUAL))
		setKey(1, 8, LayerKey(KC.QUOTE))
		setKey(1, 9, LayerKey(KC.LEFT_CURLY_BRACKET, KC.RIGHT_CURLY_BRACKET))
		setKey(1, 10, LayerKey(KC.LEFT_SQUARE_BRACKET, KC.RIGHT_SQUARE_BRACKET))
		
		setKey(2, 6, LayerKey(KC.PERCENT))
		setKey(2, 7, LayerKey(KC.SLASH, KC.BACK_SLASH))
		setKey(2, 8, LayerKey(KC.EXCLAMATION_MARK))
		setKey(2, 9, LayerKey(KC.AMPERSAND))
		setKey(2, 10, LayerKey(KC.PIPE))
		
		setKey(3, 6, LayerKey(KC.PLUS))
		setKey(3, 7, LayerKey(KC.LEFT_PARENTHESIS, KC.RIGHT_PARENTHESIS))
		
	}
	addLayer("Navigation").apply {
		setKey(0, 1, LayerKey(KC.HOME))
		setKey(0, 2, LayerKey(KC.UP))
		setKey(0, 3, LayerKey(KC.END))
		
		setKey(1, 0, LayerKey(KC.PAGE_UP))
		setKey(1, 1, LayerKey(KC.LEFT))
		setKey(1, 2, LayerKey(KC.DOWN))
		setKey(1, 3, LayerKey(KC.RIGHT))
		
		setKey(2, 0, LayerKey(KC.PAGE_DOWN))
	}
	addLayer("Numbers").apply {
		setKey(0, 7, LayerKey(KC.SEVEN))
		setKey(0, 8, LayerKey(KC.EIGHT))
		setKey(0, 9, LayerKey(KC.NINE))
		
		setKey(1, 7, LayerKey(KC.FOUR))
		setKey(1, 8, LayerKey(KC.FIVE))
		setKey(1, 9, LayerKey(KC.SIX))
		setKey(1, 10, LayerKey(KC.CIRCUMFLEX))
		
		setKey(2, 7, LayerKey(KC.ONE))
		setKey(2, 8, LayerKey(KC.TWO))
		setKey(2, 9, LayerKey(KC.THREE))
		
		setKey(3, 7, LayerKey(KC.ZERO))
	}
}
