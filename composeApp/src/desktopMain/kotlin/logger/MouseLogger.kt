package logger

import kotlinx.coroutines.flow.SharedFlow

sealed class MouseEvent
data class ButtonPressEvent(val button: MouseButton) : MouseEvent()
data class ScrollWheelEvent(val direction: ScrollDirection) : MouseEvent()
data class MovementEvent(val position: Position) : MouseEvent()

enum class MouseButton {
	LEFT,
	RIGHT,
	MIDDLE;
	
	companion object {
		fun getMouseButton(s: String): MouseButton {
			return when (s) {
				"LEFT" -> LEFT
				"RIGHT" -> RIGHT
				"MIDDLE" -> MIDDLE
				else -> throw IllegalArgumentException("Invalid mouse button")
			}
		}
	}
}

enum class ScrollDirection {
	UP,
	DOWN;
	
	companion object {
		fun getScrollDirection(s: String): ScrollDirection {
			return when (s) {
				"UP" -> UP
				"DOWN" -> DOWN
				else -> throw IllegalArgumentException("Invalid scroll direction")
			}
		}
	}
}

interface MouseLogger : Logger<MouseEvent> {
	override val dataFlow: SharedFlow<LoggerData<MouseEvent>>
}