package logger

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.datetime.LocalDateTime

/**
 * Logger interface that logs data of type T with a shared flow
 *
 * @param T
 */
interface Logger<T> {
	/**
	 * The shared flow of the data
	 */
	val dataFlow: Flow<LoggerData<T>>
	
	/**
	 * The state of the logger, whether it is running or not
	 */
	val isRunning: StateFlow<Boolean>
	
	/**
	 *  Start the logger and begin emitting data
	 */
	fun start()
	
	/**
	 * Stop the logger and stop emitting data
	 */
	fun stop()
}

data class LoggerData<T>(
	val data: T,
	val dateTime: LocalDateTime,
)