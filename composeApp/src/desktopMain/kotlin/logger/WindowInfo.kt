package logger

import database.*
import keyboard.KC
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.plus
import org.jetbrains.exposed.sql.transactions.transaction
import kotlin.time.Duration
import kotlin.time.TimeMark
import kotlin.time.TimeSource

data class Position(val x: Int, val y: Int)

fun Duration.format(): String {
	return this.toComponents { hours, minutes, seconds, _ -> "${hours}h ${minutes}m ${seconds}s" }
}

class WindowInfo(
	// Values displayed in the home cards
	val id: String,
) : Comparable<WindowInfo> {
	// The active time of the window
	var activeTime: Duration = Duration.ZERO

	val keyPresses = mutableMapOf<KC, Int>()
	val bigrams = mutableMapOf<Pair<KC, KC>, Int>()
	private var currentBigram: Pair<KC?, KC?> = Pair(null, null)

	val trigrams = mutableMapOf<Triple<KC, KC, KC>, Int>()
	private var currentTrigram: Triple<KC?, KC?, KC?> = Triple(null, null, null)

	val mouseButtons = mutableMapOf<MouseButton, Int>()
	val scrollDirections = mutableMapOf<ScrollDirection, Int>()
	val mouseMovements = mutableMapOf<Position, Int>()

	var hidden: Boolean = false

	// The time the window was last focused
	private var lastFocusedTime: TimeMark? = null

	// Update the active time of the window
	fun windowUnfocused() {
		// If the window was focused, update the active time
		lastFocusedTime?.let {
			activeTime += it.elapsedNow()
			// Reset the last focused time
			lastFocusedTime = null
		}
	}

	// Update the last focused time of the window
	fun windowFocused() {
		lastFocusedTime = TimeSource.Monotonic.markNow()
	}

	fun save() {
		// If the window was focused, update the active time before saving
		if (lastFocusedTime != null) {
			windowUnfocused()
			windowFocused()
		}

		// Upsert the window info
		transaction {
			upsertWindow()
			upsertKeyPresses()
			upsertBigrams()
			upsertTrigrams()
			upsertMousePositions()
			upsertMouseButtons()
			upsertScrollDirections()
		}
		// Reset the active time after saving
		activeTime = Duration.ZERO
	}

	private fun upsertWindow() {
		// Upsert into the Windows table
		Windows.upsert(
			Windows.id,
			onUpdate = listOf(Windows.activeTime to Windows.activeTime + activeTime),
		) {
			it[id] = this@WindowInfo.id
			it[activeTime] = this@WindowInfo.activeTime
			it[name] = this@WindowInfo.id
			it[hidden] = this@WindowInfo.hidden
		}
	}

	private fun upsertKeyPresses() {
		// Save the current key presses and clear the map
		val newKeyPresses = keyPresses.toMap()
		keyPresses.clear()

		val existingKeyPresses = KeyPresses.select { KeyPresses.windowId eq this@WindowInfo.id }
			.associateBy({ KC.getKC(it[KeyPresses.kc]) }, { it[KeyPresses.count] })

		KeyPresses.batchUpsert(newKeyPresses.keys.toList()) {
			this[KeyPresses.windowId] = this@WindowInfo.id
			this[KeyPresses.kc] = it.symbol
			this[KeyPresses.count] = (newKeyPresses[it] ?: 0) + (existingKeyPresses[it] ?: 0)
		}
	}

	private fun upsertBigrams() {
		// Save the current bigrams and clear the map
		val existingBigrams =
			Bigrams.select { Bigrams.windowId eq this@WindowInfo.id }
				.associateBy(
					{ Pair(KC.getKC(it[Bigrams.kc1]), KC.getKC(it[Bigrams.kc2])) },
					{ it[Bigrams.count] })

		Bigrams.batchUpsert(
			bigrams.keys.toList(),
		) {
			this[Bigrams.windowId] = this@WindowInfo.id
			this[Bigrams.kc1] = it.first.symbol
			this[Bigrams.kc2] = it.second.symbol
			this[Bigrams.count] = (bigrams[it] ?: 0) + (existingBigrams[it] ?: 0)
		}

		bigrams.clear()
	}

	private fun upsertTrigrams() {
		// Save the current trigrams and clear the map
		val existingTrigrams =
			Trigrams.select { Trigrams.windowId eq this@WindowInfo.id }
				.associateBy(
					{
						Triple(
							KC.getKC(it[Trigrams.kc1]),
							KC.getKC(it[Trigrams.kc2]),
							KC.getKC(it[Trigrams.kc3])
						)
					},
					{ it[Trigrams.count] })

		Trigrams.batchUpsert(
			trigrams.keys.toList(),
		) {
			this[Trigrams.windowId] = this@WindowInfo.id
			this[Trigrams.kc1] = it.first.symbol
			this[Trigrams.kc2] = it.second.symbol
			this[Trigrams.kc3] = it.third.symbol
			this[Trigrams.count] = (trigrams[it] ?: 0) + (existingTrigrams[it] ?: 0)
		}

		trigrams.clear()
	}

	private fun upsertMousePositions() {
		// Save the current mouse movements and clear the map
		val newMouseMovements = mouseMovements.toMap()
		mouseMovements.clear()

		// Fetch all the existing mouse positions for the current window
		val existingMouseMovements =
			MousePositions.select { MousePositions.windowId eq this@WindowInfo.id }
				.associateBy(
					{ Position(it[MousePositions.x], it[MousePositions.y]) },
					{ it[MousePositions.count] })

		// Upsert into the MousePositions table
		MousePositions.batchUpsert(newMouseMovements.keys.toList()) {
			this[MousePositions.windowId] = this@WindowInfo.id
			this[MousePositions.x] = it.x
			this[MousePositions.y] = it.y
			this[MousePositions.count] =
				(newMouseMovements[it] ?: 0) + (existingMouseMovements[it] ?: 0)
		}
	}

	private fun upsertMouseButtons() {
		// Save the current mouse button presses and clear the map
		val newMouseButtons = mouseButtons.toMap()
		mouseButtons.clear()

		val existingMouseButtons =
			MouseButtons.select { MouseButtons.windowId eq this@WindowInfo.id }
				.associateBy(
					{ MouseButton.valueOf(it[MouseButtons.button]) },
					{ it[MouseButtons.count] })

		MouseButtons.batchUpsert(newMouseButtons.keys.toList()) {
			this[MouseButtons.windowId] = this@WindowInfo.id
			this[MouseButtons.button] = it.name
			this[MouseButtons.count] =
				(newMouseButtons[it] ?: 0) + (existingMouseButtons[it] ?: 0)
		}
	}

	private fun upsertScrollDirections() {
		// Save the current scroll directions and clear the map
		val oldScrollDirections = scrollDirections.toMap()
		scrollDirections.clear()

		// Fetch all the existing scroll directions for the current window
		val existingScrollDirections =
			ScrollDirections.select { ScrollDirections.windowId eq this@WindowInfo.id }
				.associateBy(
					{ ScrollDirection.valueOf(it[ScrollDirections.direction]) },
					{ it[ScrollDirections.count] })

		// Upsert into the ScrollDirections table
		ScrollDirections.batchUpsert(oldScrollDirections.keys.toList()) {
			this[ScrollDirections.windowId] = this@WindowInfo.id
			this[ScrollDirections.direction] = it.name
			this[ScrollDirections.count] =
				(oldScrollDirections[it] ?: 0) + (existingScrollDirections[it] ?: 0)
		}
	}

	// Add a key press to the keyPresses map
	fun addKeyPress(key: KC) {
		keyPresses[key] = keyPresses.getOrDefault(key, 0) + 1

		// Update the bigram
		currentBigram = Pair(currentBigram.second, key)
		if (currentBigram.first != null && currentBigram.second != null) {
			bigrams[currentBigram as Pair<KC, KC>] =
				bigrams.getOrDefault(currentBigram, 0) + 1
		}

		// Update the trigram
		currentTrigram = Triple(currentTrigram.second, currentTrigram.third, key)
		if (currentTrigram.first != null && currentTrigram.second != null && currentTrigram.third != null) {
			trigrams[currentTrigram as Triple<KC, KC, KC>] =
				trigrams.getOrDefault(currentTrigram, 0) + 1
		}
	}

	// Add a mouse button press to the mouseButtons map
	fun addMouseButtonPress(button: MouseButton) {
		mouseButtons[button] = mouseButtons.getOrDefault(button, 0) + 1
	}

	// Add a scroll wheel event to the scrollDirections map
	fun addScrollWheelEvent(direction: ScrollDirection) {
		scrollDirections[direction] = scrollDirections.getOrDefault(direction, 0) + 1
	}

	// Add a mouse movement event to the mouseMovements map
	fun addMouseMovement(position: Position) {
		mouseMovements[position] = mouseMovements.getOrDefault(position, 0) + 1
	}

	override fun compareTo(other: WindowInfo): Int {
		return activeTime.compareTo(other.activeTime)
	}

	override fun equals(other: Any?): Boolean {
		if (this === other) return true
		if (other !is WindowInfo) return false

		return id == other.id
	}

	override fun hashCode(): Int {
		return id.hashCode()
	}

	override fun toString(): String {
		return "Name: $id\n, Active Time: $activeTime\n"
	}
}