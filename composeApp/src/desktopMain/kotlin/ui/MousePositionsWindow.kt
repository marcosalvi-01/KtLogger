package ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.window.WindowDraggableArea
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asComposeImageBitmap
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.WindowState
import icon
import logger.Position
import org.jetbrains.skia.Bitmap
import org.jetbrains.skia.ColorAlphaType
import org.jetbrains.skia.ColorType
import org.jetbrains.skia.ImageInfo

@Composable
fun MousePositionsWindow(
	isMousePositionsOpen: MutableState<Boolean>,
	mousePositions: Map<Position, Int>,
) {
	val windowState = WindowState(width = 1000.dp, height = 400.dp)
	// Draw a window that represent the screen and color each pixel based on the amount of time the mouse was on that pixel
	Window(
		onCloseRequest = {
			isMousePositionsOpen.value = false
		},
		state = windowState,
		title = "Mouse Positions",
		icon = icon,
		visible = isMousePositionsOpen.value,
		transparent = true,
		undecorated = true
	) {
		Surface(
			modifier = Modifier.fillMaxSize().padding(5.dp).shadow(3.dp, RoundedCornerShape(10.dp)),
			color = MaterialTheme.colors.background,
			shape = RoundedCornerShape(10.dp)
		) {
			Column(
				modifier = Modifier.fillMaxSize()
			) {
				WindowDraggableArea {
					TopAppBar(
						title = { Text("Mouse Positions Heatmap") },
						actions = {
							// Close the window
							IconButton(onClick = {
								isMousePositionsOpen.value = false
							}) {
								Icon(
									imageVector = Icons.Rounded.Close,
									contentDescription = "Close",
									tint = MaterialTheme.colors.onBackground
								)
							}
						}
					)
				}
				// calculate the image only once
				val bitmap = remember { createHeatmapBitmap(mousePositions) }
				Canvas(Modifier.fillMaxSize()) {
					val imageWidth = bitmap.width.toFloat()
					val imageHeight = bitmap.height.toFloat()

					val canvasWidth = size.width
					val canvasHeight = size.height

					val scaleX = canvasWidth / imageWidth
					val scaleY = canvasHeight / imageHeight

					val scale = minOf(scaleX, scaleY)

					drawIntoCanvas { canvas ->
						canvas.scale(scale)
						drawImage(
							image = bitmap,
						)
					}
				}
			}
		}
	}
}

// Create a Bitmap with the heatmap
// each pixel will be colored based on the amount of time the mouse was on that pixel
private fun createHeatmapBitmap(
	mousePositions: Map<Position, Int>,
): ImageBitmap {
	val maxTimes = mousePositions.values.maxOrNull() ?: 1
	val height = 1440
	val width = 3440 + 1920

	val bitmap = Bitmap().apply {
		setImageInfo(ImageInfo(width, height, ColorType.BGRA_8888, ColorAlphaType.PREMUL))
		allocPixels()
	}

	val byteArray = ByteArray(width * height * 4)

	for ((position, times) in mousePositions) {
		if (position.x < width && position.y < height) {
			val color = interpolateColor(times.toFloat() / maxTimes).toArgb()
			val index = (position.y * width + position.x) * 4

			// Calculate the color components
			val blue = color and 0xFF
			val green = (color shr 8) and 0xFF
			val red = (color shr 16) and 0xFF

			byteArray[index] = blue.toByte()
			byteArray[index + 1] = green.toByte()
			byteArray[index + 2] = red.toByte()
			byteArray[index + 3] = 255.toByte()
		}
	}

	bitmap.installPixels(byteArray)

	return bitmap.asComposeImageBitmap()
}

