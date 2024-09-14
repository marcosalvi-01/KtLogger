package database

import keyboard.AbstractKeymap
import keyboard.KC
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import logger.MouseButton
import logger.Position
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.andWhere
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.update
import org.jetbrains.exposed.sql.upsert
import kotlin.time.Duration

object Database {
	private val _windows = MutableSharedFlow<Map<Pair<String, String>, Duration>>()
	val windows = _windows.asSharedFlow()
	
	// Lazy initialization because the db needs to be connected first
	private val _totalTime by lazy { MutableStateFlow(getFocusTime()) }
	val totalTime by lazy { _totalTime.asStateFlow() }
	
	fun connect() {
		// Connect to the database
		Database.connect("jdbc:sqlite:data.db", driver = "org.sqlite.JDBC")
		
		transaction {
			// Create the tables if they don't exist
			SchemaUtils.create(
				Windows,
				KeyPresses,
				MousePositions,
				MouseButtons,
				ScrollDirections,
				Bigrams,
				Trigrams,
				Selected,
				Keymaps,
			)
		}
	}
	
	fun setWindowName(id: String, name: String) {
		transaction {
			Windows.update({ Windows.id eq id }) {
				it[this.name] = name
			}
		}
	}
	
	suspend fun loadData() {
		loadWindowsSuspend()
		getFocusTimeSuspend()
	}
	
	fun loadWindows(): Map<Pair<String, String>, Duration> =
		transaction {
			Windows.selectAll(
			).where(
				Windows.hidden eq false
			).associate {
				Pair(it[Windows.id], it[Windows.name]) to it[Windows.activeTime]
			}
		}
	
	// Updates the loaded windows
	private suspend fun loadWindowsSuspend() {
		_windows.emit(loadWindows())
	}
	
	// Get the focus time without filtering by window (all the focus time in total)
	fun getFocusTime(): Duration {
		return transaction {
			Windows.selectAll().map { it[Windows.activeTime] }
				.reduceOrNull { acc, duration -> acc + duration } ?: Duration.ZERO
			
		}
	}
	
	private suspend fun getFocusTimeSuspend() {
		_totalTime.emit(getFocusTime())
	}
	
	fun getFocusTime(windowId: String): Duration {
		return transaction {
			Windows.selectAll().where { Windows.id eq windowId }
				.map { it[Windows.activeTime] }
				.reduceOrNull { acc, duration -> acc + duration } ?: Duration.ZERO
		}
	}
	
	fun getFocusTime(windowIds: List<String>): Duration {
		return transaction {
			Windows.selectAll().where { Windows.id inList windowIds }
				.map { it[Windows.activeTime] }
				.reduceOrNull { acc, duration -> acc + duration } ?: Duration.ZERO
		}
	}
	
	// Get the keypresses without filtering by window (all the keypresses in total)
	fun getKeyPresses(): Map<KC, Int> {
		return transaction {
			KeyPresses.selectAll().groupBy {
				it[KeyPresses.kc]
			}.mapValues { entry ->
				entry.value.sumOf { it[KeyPresses.count] }
			}.mapKeys { KC.getKC(it.key) }
		}
	}
	
	fun getKeyPresses(windowId: String): Map<KC, Int> {
		return transaction {
			KeyPresses.selectAll().where { KeyPresses.windowId eq windowId }
				.associate { KC.getKC(it[KeyPresses.kc]) to it[KeyPresses.count] }
		}
	}
	
	fun getKeyPresses(windowIds: List<String>): Map<KC, Int> {
		return transaction {
			KeyPresses.selectAll().where { KeyPresses.windowId inList windowIds }
				.groupBy { it[KeyPresses.kc] }
				.mapValues { entry ->
					entry.value.sumOf { it[KeyPresses.count] }
				}.mapKeys { KC.getKC(it.key) }
		}
	}
	
	// Get the bigrams without filtering by window (all the bigrams in total)
	fun getBigrams(): Map<Pair<KC, KC>, Int> {
		return transaction {
			Bigrams.selectAll().groupBy {
				it[Bigrams.kc1] to it[Bigrams.kc2]
			}.mapValues { entry ->
				entry.value.sumOf { it[Bigrams.count] }
			}.mapKeys {
				Pair(
					KC.getKC(it.key.first),
					KC.getKC(it.key.second)
				)
			}
		}
	}
	
	fun getBigrams(windowId: String): Map<Pair<KC, KC>, Int> {
		return transaction {
			Bigrams.selectAll().where { Bigrams.windowId eq windowId }
				.associate {
					Pair(
						KC.getKC(it[Bigrams.kc1]),
						KC.getKC(it[Bigrams.kc2])
					) to it[Bigrams.count]
				}
		}
	}
	
	fun getBigrams(windowIds: List<String>): Map<Pair<KC, KC>, Int> {
		return transaction {
			Bigrams.selectAll().where { Bigrams.windowId inList windowIds }
				.groupBy { it[Bigrams.kc1] to it[Bigrams.kc2] }
				.mapValues { entry ->
					entry.value.sumOf { it[Bigrams.count] }
				}.mapKeys {
					Pair(
						KC.getKC(it.key.first),
						KC.getKC(it.key.second)
					)
				}
		}
	}
	
	// Get the trigrams without filtering by window (all the trigrams in total)
	fun getTrigrams(): Map<Triple<KC, KC, KC>, Int> {
		return transaction {
			Trigrams.selectAll().groupBy {
				Triple(
					KC.getKC(it[Trigrams.kc1]),
					KC.getKC(it[Trigrams.kc2]),
					KC.getKC(it[Trigrams.kc3])
				)
			}.mapValues { entry ->
				entry.value.sumOf { it[Trigrams.count] }
			}
		}
	}
	
	fun getTrigrams(windowId: String): Map<Triple<KC, KC, KC>, Int> {
		return transaction {
			Trigrams.selectAll().where { Trigrams.windowId eq windowId }
				.associate {
					Triple(
						KC.getKC(it[Trigrams.kc1]),
						KC.getKC(it[Trigrams.kc2]),
						KC.getKC(it[Trigrams.kc3])
					) to it[Trigrams.count]
				}
		}
	}
	
	fun getTrigrams(windowIds: List<String>): Map<Triple<KC, KC, KC>, Int> {
		return transaction {
			Trigrams.selectAll().where { Trigrams.windowId inList windowIds }
				.groupBy {
					Triple(
						KC.getKC(it[Trigrams.kc1]),
						KC.getKC(it[Trigrams.kc2]),
						KC.getKC(it[Trigrams.kc3])
					)
				}.mapValues { entry ->
					entry.value.sumOf { it[Trigrams.count] }
				}
		}
	}
	
	fun getMousePositions(): Map<Position, Int> {
		return transaction {
			MousePositions.selectAll().associate {
				Position(
					it[MousePositions.x],
					it[MousePositions.y]
				) to it[MousePositions.count]
			}
		}
	}
	
	fun getMousePositions(windowId: String): Map<Position, Int> {
		return transaction {
			MousePositions.selectAll().where { MousePositions.windowId eq windowId }
				.associate {
					Position(
						it[MousePositions.x],
						it[MousePositions.y]
					) to it[MousePositions.count]
				}
		}
	}
	
	fun getMousePositions(windowIds: List<String>): Map<Position, Int> {
		return transaction {
			MousePositions.selectAll().where { MousePositions.windowId inList windowIds }
				.associate {
					Position(
						it[MousePositions.x],
						it[MousePositions.y]
					) to it[MousePositions.count]
				}
		}
	}
	
	// Get the mouse buttons without filtering by window (all the mouse buttons in total)
	fun getMouseButtons(): Map<MouseButton, Int> {
		return transaction {
			MouseButtons.selectAll().groupBy {
				MouseButton.getMouseButton(it[MouseButtons.button])
			}.mapValues { entry ->
				entry.value.sumOf { it[MouseButtons.count] }
			}
		}
	}
	
	fun getMouseButtons(windowId: String): Map<MouseButton, Int> {
		return transaction {
			MouseButtons.selectAll().where { MouseButtons.windowId eq windowId }
				.associate { MouseButton.getMouseButton(it[MouseButtons.button]) to it[MouseButtons.count] }
		}
	}
	
	fun getMouseButtons(windowIds: List<String>): Map<MouseButton, Int> {
		return transaction {
			MouseButtons.selectAll().where { MouseButtons.windowId inList windowIds }
				.groupBy { MouseButton.getMouseButton(it[MouseButtons.button]) }
				.mapValues { entry ->
					entry.value.sumOf { it[MouseButtons.count] }
				}
		}
	}
	
	fun getScrollDirections(): Map<logger.ScrollDirection, Int> {
		return transaction {
			ScrollDirections.selectAll().groupBy {
				logger.ScrollDirection.getScrollDirection(it[ScrollDirections.direction])
			}.mapValues { entry ->
				entry.value.sumOf { it[ScrollDirections.count] }
			}
		}
	}
	
	fun getScrollDirections(windowId: String): Map<logger.ScrollDirection, Int> {
		return transaction {
			ScrollDirections.selectAll().where { ScrollDirections.windowId eq windowId }
				.associate { logger.ScrollDirection.getScrollDirection(it[ScrollDirections.direction]) to it[ScrollDirections.count] }
		}
	}
	
	fun getScrollDirections(windowIds: List<String>): Map<logger.ScrollDirection, Int> {
		return transaction {
			ScrollDirections.selectAll().where { ScrollDirections.windowId inList windowIds }
				.groupBy { logger.ScrollDirection.getScrollDirection(it[ScrollDirections.direction]) }
				.mapValues { entry ->
					entry.value.sumOf { it[ScrollDirections.count] }
				}
		}
	}
	
	fun hideWindow(window: String) {
		transaction {
			Windows.update({ Windows.id eq window }) {
				it[hidden] = true
			}
		}
	}
	
	fun selectWindow(window: String) {
		// insert the window and update the value of "selected" to true
		transaction {
			Selected.upsert(Selected.windowId) {
				it[windowId] = window
				it[selected] = true
			}
		}
	}
	
	fun deselectWindow(window: String) {
		// update the value of "selected" to false
		transaction {
			Selected.upsert(Selected.windowId) {
				it[windowId] = window
				it[selected] = false
			}
		}
	}
	
	// Return only the windows that have the value of "selected" as true
	fun getSelectedWindows(): List<String> {
		return transaction {
			Selected.selectAll().andWhere { Selected.selected eq true }
				.map { it[Selected.windowId] }
		}
	}
	
	fun deselectAllWindows() {
		transaction {
			Selected.update { it[selected] = false }
		}
	}
	
	fun createKeymap(keymap: AbstractKeymap) {
		transaction {
			Keymaps.insert {
				it[name] = keymap.name
				it[Keymaps.keymap] = keymap
			}
		}
	}
	
	fun getKeymap(name: String): AbstractKeymap? {
		return transaction {
			Keymaps.selectAll().where { Keymaps.name eq name }
				.map { it[Keymaps.keymap] }
				.firstOrNull()
		}
	}
	
	fun deleteKeymap(name: String) {
		transaction {
			Keymaps.deleteWhere { Keymaps.name eq name }
		}
	}
	
	fun updateKeymap(keymap: AbstractKeymap) {
		transaction {
			Keymaps.update({ Keymaps.name eq keymap.name }) {
				it[Keymaps.keymap] = keymap
			}
		}
	}
	
	fun getKeymaps(): List<AbstractKeymap> {
		return transaction {
			Keymaps.selectAll().map { it[Keymaps.keymap] }
		}
	}
	
	fun getKeymapsNames(): List<String> {
		return transaction {
			Keymaps.selectAll().map { it[Keymaps.name] }
		}
	}
}