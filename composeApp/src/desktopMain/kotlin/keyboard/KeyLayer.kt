package keyboard

// Represent a layer in a keymap.
// Each layer is a matrix of keys, where each key has a kc and a shifted kc.
data class KeyLayer(
	val keys: List<List<LayerKey?>>,
) {
	fun getWidth() = keys[0].size
	fun getHeight() = keys.size

	fun getKc(row: Int, col: Int): KC {
		if (row < 0 || row >= getHeight() || col < 0 || col >= getWidth())
			return KC.UNKNOWN
		return keys[row][col]?.kc ?: KC.UNKNOWN
	}

	fun contains(kc: KC): Boolean {
		for (row in keys)
			for (key in row)
				if (key?.kc == kc)
					return true
		return false
	}
}

data class LayerKey(
	val kc: KC,
	val shiftedKC: KC? = null,
)

data class Keymap(
	val layers: List<KeyLayer>,
)