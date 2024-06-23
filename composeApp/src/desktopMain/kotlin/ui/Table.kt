package ui

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun RowScope.TableCell(
	text: String,
	weight: Float,
	borderColor: Color = MaterialTheme.colors.background,
	backgroundColor: Color = MaterialTheme.colors.surface,
) {
	Text(
		text = text,
		Modifier
			.border(1.dp, borderColor)
			.weight(weight)
			.background(backgroundColor)
			.padding(8.dp)
	)
}

@Composable
fun TableRow(
	rowData: RowItem,
	borderColor: Color = MaterialTheme.colors.background,
	backgroundColor: Color = MaterialTheme.colors.surface,
	mapDataToTableCells: @Composable RowScope.(RowItem) -> Unit,
) {
	Row(
		Modifier
			.background(backgroundColor)
			.border(1.dp, borderColor)
	) {
		mapDataToTableCells(rowData)
	}
}

interface RowItem {
	fun getColumns(): List<ColumnItem>
}

interface ColumnItem {
	fun getName(): String
	fun getWeight(): Float
}

@Composable
fun Table(
	columns: List<ColumnItem>,
	data: List<RowItem>,
	borderColor: Color = MaterialTheme.colors.background,
	backgroundColor: Color = MaterialTheme.colors.surface,
	headerBackgroundColor: Color = MaterialTheme.colors.secondary,
	headerBorderColor: Color = MaterialTheme.colors.background,
	showScrollbar: Boolean = false,
	mapDataToTableCells: @Composable RowScope.(RowItem) -> Unit,
) {
	Column {
		// The header row
		Row {
			for (column in columns) {
				TableCell(
					text = column.getName(),
					weight = column.getWeight(),
					borderColor = headerBorderColor,
					backgroundColor = headerBackgroundColor
				)
			}
		}

		Box {
			val scrollState = rememberLazyListState(0)
			// The LazyColumn for the data rows
			LazyColumn(
				state = scrollState,
			) {
				// Here are all the lines of your table.
				items(data) { rowData ->
					TableRow(
						rowData = rowData,
						borderColor = borderColor,
						backgroundColor = backgroundColor,
						mapDataToTableCells = mapDataToTableCells
					)
				}
			}

			if (showScrollbar)
			// The vertical scrollbar
				VerticalScrollbar(
					modifier = Modifier.align(Alignment.CenterEnd).fillMaxHeight(),
					adapter = rememberScrollbarAdapter(scrollState)
				)
		}
	}
}

class KeyRowItem(private val columns: List<ColumnItem>) : RowItem {
	override fun getColumns(): List<ColumnItem> = columns
}

class KeyColumnItem(private val name: String, private val weight: Float) : ColumnItem {
	override fun getName(): String = name
	override fun getWeight(): Float = weight
}

