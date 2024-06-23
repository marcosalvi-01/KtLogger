package database


import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.kotlin.datetime.duration

object Windows : Table() {
	// The original name of the process
	val id = varchar("id", length = 255)
	val activeTime = duration("active_time")
	val hidden = bool("hidden").default(false)

	// The name to be displayed (by default the same as the id,
	val name = varchar("name", length = 255)

	override val primaryKey = PrimaryKey(id)
}

object KeyPresses : Table() {
	val windowId = reference("window_id", Windows.id)
	val kc = varchar("kc", length = 32)
	val count = integer("count")

	override val primaryKey = PrimaryKey(windowId, kc)
}

object MousePositions : Table() {
	val windowId = reference("window_id", Windows.id)
	val x = integer("x")
	val y = integer("y")
	val count = integer("count")

	override val primaryKey = PrimaryKey(windowId, x, y)
}

object MouseButtons : Table() {
	val windowId = reference("window_id", Windows.id)
	val button = varchar("button", length = 255)
	val count = integer("count")

	override val primaryKey = PrimaryKey(windowId, button)
}

object ScrollDirections : Table() {
	val windowId = reference("window_id", Windows.id)
	val direction = varchar("direction", length = 255)
	val count = integer("count")

	override val primaryKey = PrimaryKey(windowId, direction)
}

object Bigrams : Table() {
	val windowId = reference("window_id", Windows.id)
	val kc1 = varchar("kc1", length = 32)
	val kc2 = varchar("kc2", length = 32)
	val count = integer("count")

	override val primaryKey = PrimaryKey(windowId, kc1, kc2)
}

object Trigrams : Table() {
	val windowId = reference("window_id", Windows.id)
	val kc1 = varchar("kc1", length = 32)
	val kc2 = varchar("kc2", length = 32)
	val kc3 = varchar("kc3", length = 32)
	val count = integer("count")

	override val primaryKey = PrimaryKey(windowId, kc1, kc2, kc3)
}

// The windows selected by the user in the UI
object Selected : Table() {
	val windowId = reference("window_id", Windows.id)
	val selected = bool("selected")

	override val primaryKey = PrimaryKey(windowId)
}
