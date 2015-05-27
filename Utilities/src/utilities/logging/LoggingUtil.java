package utilities.logging;

import org.apache.logging.log4j.Marker;
import org.apache.logging.log4j.MarkerManager;

/**
 * Class that provides helpful utilities for the use of log4j.
 */
public class LoggingUtil {

	/**
	 * Special {@link Marker} for filtering important system logging events such as the current tick and time.
	 */
	public static final Marker SYSTEM_MARKER = MarkerManager.getMarker("SYSTEM");

	/**
	 * Private constructor to prevent object creation.
	 */
	private LoggingUtil() {
		// prevent object creation
	}
}