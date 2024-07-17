package keyboard

import kotlinx.serialization.Serializable

@Serializable
data class LayerKey(
	val kc: KC,
	val shiftedKC: KC? = null,
)

@Serializable
sealed class AbstractKeymap {
	abstract val name: String
	abstract val rows: Int
	abstract val cols: Int
	abstract val layers: List<AbstractKeyLayer>
	
	abstract fun addLayer(name: String): AbstractKeyLayer
	abstract fun removeLayer(name: String)
	fun getLayer(name: String): AbstractKeyLayer? = layers.firstOrNull { it.name == name }
}

@Serializable
sealed class AbstractKeyLayer {
	abstract val name: String
	abstract val rows: Int
	abstract val cols: Int
	abstract val matrix: Array<Array<LayerKey?>>
	
	open fun getKey(row: Int, col: Int): LayerKey? {
		if (row < 0 || row >= matrix.size || col < 0 || col >= matrix[0].size) return null
		return matrix[row][col]
	}
	
	open fun contains(kc: KC): Boolean {
		for (row in matrix) for (key in row) if (key?.kc == kc || key?.shiftedKC == kc) return true
		return false
	}
	
	open fun setKey(row: Int, col: Int, key: LayerKey) {
		if (row < 0 || row >= matrix.size || col < 0 || col >= matrix[0].size) return
		matrix[row][col] = key
	}
}

@Serializable
data class Keymap(
	override val name: String,
	override val rows: Int,
	override val cols: Int,
) : AbstractKeymap() {
	private val _layers = mutableListOf<KeyLayer>()
	override val layers: List<KeyLayer> = _layers
	
	override fun addLayer(name: String): KeyLayer {
		val layer = KeyLayer(name, rows, cols)
		_layers.add(layer)
		return layer
	}
	
	override fun removeLayer(name: String) {
		_layers.removeIf { it.name == name }
	}
}

@Serializable
data class KeyLayer(
	override val name: String,
	override val rows: Int,
	override val cols: Int,
) : AbstractKeyLayer() {
	override val matrix: Array<Array<LayerKey?>> =
		Array(rows) { Array(cols) { LayerKey(KC.EMPTY) } }
}

@Serializable
data class SplitKeymap(
	override val name: String,
	val sideRows: Int,
	val sideCols: Int,
	val thumbs: Int,
) : AbstractKeymap() {
	private val _layers = mutableListOf<SplitKeyLayer>()
	override val layers: List<SplitKeyLayer> = _layers
	override val rows = sideRows + 1
	override val cols = (sideCols * 2) + 1
	
	override fun addLayer(name: String): SplitKeyLayer {
		val layer = SplitKeyLayer(name, sideRows, sideCols, thumbs)
		_layers.add(layer)
		return layer
	}
	
	override fun removeLayer(name: String) {
		_layers.removeIf { it.name == name }
	}
}

@Serializable
data class SplitKeyLayer(
	override val name: String,
	private val sideRows: Int,
	private val sideCols: Int,
	val thumbs: Int,
) : AbstractKeyLayer() {
	override val rows = sideRows + 1
	override val cols = (sideCols * 2) + 1
	
	init {
		require(thumbs <= sideCols) { "The number of thumbs must be less than or equal to the number of columns" }
	}
	
	override val matrix: Array<Array<LayerKey?>> = Array(sideRows + 1) { rowIndex ->
		Array((sideCols * 2) + 1) { colIndex ->
			when {
				colIndex == sideCols -> null // Set the middle column to null
				rowIndex == sideRows -> { // For the last row (thumb row)
					val middle = sideCols // Middle column index
					val start = middle - (thumbs) // Starting index for thumb keys
					val end = middle + (thumbs) // Ending index for thumb keys
					if (colIndex in start..end) LayerKey(KC.EMPTY) else null
				}
				
				else -> LayerKey(KC.EMPTY) // For all other cells
			}
		}
	}
}

