package ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asComposeImageBitmap
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.toArgb
import logger.Position
import org.jetbrains.skia.Bitmap
import org.jetbrains.skia.ColorAlphaType
import org.jetbrains.skia.ColorType
import org.jetbrains.skia.ImageInfo
import ui.heatmap.interpolateColor

@Composable
fun MousePositionsWindow(
	mousePositions: Map<Position, Int>,
) {
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

